package io.animebay.stream.ui.screens.player.controls

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import kotlinx.coroutines.delay

// --- الألوان ---
private val PrimaryGreen = Color(0xFF4CAF50)
private val ControlsBackground = Color.Black.copy(alpha = 0.6f)
private val LightGreen = Color(0xFFC8E6C9)

@Composable
fun ProfessionalPlayerControls(
    player: Player,
    title: String,
    episode: String,
    onBackClick: () -> Unit,
    isFullScreen: Boolean,
    onFullScreenToggle: () -> Unit
) {
    var controlsVisible by remember { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(player.isPlaying) }
    var totalDuration by remember { mutableStateOf(0L) }
    var currentTime by remember { mutableStateOf(0L) }
    var playbackState by remember { mutableStateOf(player.playbackState) }

    // Listener لتحديث حالة المشغل
    LaunchedEffect(player) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlayingValue: Boolean) {
                isPlaying = isPlayingValue
            }
            override fun onPlaybackStateChanged(playbackStateValue: Int) {
                playbackState = playbackStateValue
            }
            override fun onEvents(player: Player, events: Player.Events) {
                totalDuration = player.duration.coerceAtLeast(0L)
            }
        }
        player.addListener(listener)

        // تحديث الوقت الحالي بشكل دوري
        while (true) {
            currentTime = player.currentPosition.coerceAtLeast(0L)
            delay(500)
        }
    }

    // إخفاء عناصر التحكم تلقائياً
    LaunchedEffect(controlsVisible, isPlaying) {
        if (controlsVisible && isPlaying) {
            delay(5000)
            controlsVisible = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                controlsVisible = !controlsVisible
            }
    ) {
        // شريط التقدم المصغر عند إخفاء عناصر التحكم
        AnimatedVisibility(
            visible = !controlsVisible && isPlaying,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            MinimalProgressBar(currentTime, totalDuration)
        }

        // عناصر التحكم الكاملة
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier.background(
                    Brush.verticalGradient(
                        colors = listOf(
                            ControlsBackground,
                            Color.Transparent,
                            ControlsBackground
                        )
                    )
                )
            ) {
                // عناصر التحكم العلوية
                TopControls(
                    title = title,
                    episode = episode,
                    onBackClick = onBackClick,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                )

                // عناصر التحكم في المنتصف
                CenterControls(
                    player = player,
                    isPlaying = isPlaying,
                    playbackState = playbackState,
                    modifier = Modifier.align(Alignment.Center)
                )

                // عناصر التحكم السفلية
                BottomControls(
                    player = player,
                    currentTime = currentTime,
                    totalDuration = totalDuration,
                    onFullScreenToggle = onFullScreenToggle,
                    isFullScreen = isFullScreen,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                )
            }
        }
    }
}

@Composable
private fun MinimalProgressBar(currentTime: Long, totalDuration: Long) {
    val progress = if (totalDuration > 0) (currentTime.toFloat() / totalDuration).coerceIn(0f, 1f) else 0f
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = 0.2f))
        )
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress)
                .background(PrimaryGreen)
        )
    }
}

@Composable
private fun TopControls(
    title: String,
    episode: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ControlButton(icon = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", onClick = onBackClick)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
            Text("الحلقة $episode", color = LightGreen, maxLines = 1)
        }
    }
}

@Composable
private fun CenterControls(
    player: Player,
    isPlaying: Boolean,
    playbackState: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ControlButton(icon = Icons.Default.Replay10, "ترجيع 10 ثواني", { player.seekBack() }, size = 56.dp)
        
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
            if (playbackState == Player.STATE_BUFFERING) {
                CircularProgressIndicator(color = PrimaryGreen, strokeWidth = 3.dp)
            } else {
                ControlButton(
                    icon = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "إيقاف" else "تشغيل",
                    onClick = { if (player.isPlaying) player.pause() else player.play() },
                    size = 80.dp,
                    backgroundColor = PrimaryGreen.copy(alpha = 0.8f)
                )
            }
        }
        
        ControlButton(icon = Icons.Default.Forward10, "تقديم 10 ثواني", { player.seekForward() }, size = 56.dp)
    }
}

@Composable
private fun BottomControls(
    player: Player,
    currentTime: Long,
    totalDuration: Long,
    onFullScreenToggle: () -> Unit,
    isFullScreen: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(formatDuration(currentTime), color = Color.White, fontSize = 14.sp)
            Slider(
                value = currentTime.toFloat(),
                onValueChange = { player.seekTo(it.toLong()) },
                valueRange = 0f..totalDuration.toFloat().coerceAtLeast(1f),
                colors = SliderDefaults.colors(thumbColor = PrimaryGreen, activeTrackColor = PrimaryGreen, inactiveTrackColor = Color.Gray.copy(alpha = 0.3f)),
                modifier = Modifier.weight(1f)
            )
            Text(formatDuration(totalDuration), color = Color.White, fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.weight(1f))
            ControlButton(if (isFullScreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen, "ملء الشاشة", onFullScreenToggle, size = 40.dp)
        }
    }
}

@Composable
private fun ControlButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    size: Dp = 48.dp,
    backgroundColor: Color = Color.Black.copy(alpha = 0.4f)
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription, tint = Color.White, modifier = Modifier.size(size * 0.5f))
    }
}

private fun formatDuration(millis: Long): String {
    val totalSeconds = millis / 1000
    val seconds = totalSeconds % 60
    val minutes = (totalSeconds / 60) % 60
    val hours = totalSeconds / 3600
    return if (hours > 0) String.format("%d:%02d:%02d", hours, minutes, seconds)
    else String.format("%02d:%02d", minutes, seconds)
}

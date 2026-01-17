package io.animebay.stream.ui.screens.player

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.util.MimeTypes
import io.animebay.stream.ui.screens.player.viewmodel.PlayerViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PlayerScreen(
    videoUrl: String,
    episodeTitle: String,
    onBackPressed: () -> Unit,
    playerViewModel: PlayerViewModel = viewModel()
) {
    val context = LocalContext.current
    var player by remember { mutableStateOf<ExoPlayer?>(null) }
    var isControlsVisible by remember { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(true) }
    var currentTime by remember { mutableStateOf(0L) }
    var totalDuration by remember { mutableStateOf(0L) }
    var isBuffering by remember { mutableStateOf(false) }
    var isFullscreen by remember { mutableStateOf(false) }
    var playbackSpeed by remember { mutableStateOf(1.0f) }
    var isMuted by remember { mutableStateOf(false) }
    var showSpeedMenu by remember { mutableStateOf(false) }
    var showQualityMenu by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(videoUrl) {
        if (player == null) {
            val trackSelector = DefaultTrackSelector(context).apply {
                setParameters(buildUponParameters().setMaxVideoSize(1920, 1080))
            }
            
            player = ExoPlayer.Builder(context)
                .setTrackSelector(trackSelector)
                .build()
                .apply {
                    val mediaItem = MediaItem.Builder()
                        .setUri(videoUrl)
                        .setMimeType(MimeTypes.APPLICATION_M3U8)
                        .build()
                    
                    val dataSourceFactory = DefaultHttpDataSource.Factory()
                        .setDefaultRequestProperties(
                            mapOf(
                                "User-Agent" to "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Mobile Safari/537.36",
                                "Referer" to "https://witanime.red/"
                            )
                        )
                    
                    val mediaSource = DefaultMediaSourceFactory(dataSourceFactory)
                        .createMediaSource(mediaItem)
                    
                    setMediaSource(mediaSource)
                    prepare()
                    play()
                    
                    addListener(object : Player.Listener {
                        override fun onPlaybackStateChanged(playbackState: Int) {
                            isBuffering = playbackState == Player.STATE_BUFFERING
                        }
                        
                        override fun onIsPlayingChanged(playing: Boolean) {
                            isPlaying = playing
                        }
                        
                        override fun onPositionDiscontinuity(
                            oldPosition: Player.PositionInfo,
                            newPosition: Player.PositionInfo,
                            reason: Int
                        ) {
                            currentTime = currentPosition
                            totalDuration = duration
                        }
                    })
                }
        }
    }

    // إخفاء عناصر التحكم تلقائياً
    LaunchedEffect(isControlsVisible, isPlaying) {
        if (isControlsVisible && isPlaying) {
            delay(5000)
            isControlsVisible = false
        }
    }

    // تحديث الوقت الحالي بشكل دوري
    LaunchedEffect(player) {
        player?.let { exoPlayer ->
            while (true) {
                currentTime = exoPlayer.currentPosition.coerceAtLeast(0L)
                totalDuration = exoPlayer.duration.coerceAtLeast(0L)
                delay(500)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            player?.release()
            player = null
        }
    }

    LaunchedEffect(isFullscreen) {
        val activity = context as? Activity
        if (activity != null) {
            if (isFullscreen) {
                activity.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                activity.window.decorView.systemUiVisibility = (
                    android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
                    or android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                )
            } else {
                activity.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                activity.window.decorView.systemUiVisibility = android.view.View.SYSTEM_UI_FLAG_VISIBLE
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { ctx ->
                StyledPlayerView(ctx).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    player = this@PlayerScreen.player
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // شريط التقدم المصغر عند إخفاء عناصر التحكم
        AnimatedVisibility(
            visible = !isControlsVisible && isPlaying,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            MinimalProgressBar(currentTime, totalDuration)
        }

        // عناصر التحكم الكاملة
        AnimatedVisibility(
            visible = isControlsVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { isControlsVisible = !isControlsVisible }
                        )
                    }
            ) {
                // الأيقونات العلوية
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.TopCenter)
                ) {
                    IconButton(
                        onClick = onBackPressed,
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = Color.White
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = episodeTitle,
                        color = Color.White,
                        fontSize = 16.sp,
                        maxLines = 1
                    )
                }

                // أزرار التحكم في المنتصف
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
                        .padding(horizontal = 32.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // تراجع 10 ثواني
                    IconButton(
                        onClick = {
                            player?.let {
                                val newPosition = maxOf(0, it.currentPosition - 10000)
                                it.seekTo(newPosition)
                            }
                        },
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            Icons.Default.Replay10,
                            contentDescription = "تراجع 10 ثواني",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // زر التشغيل/الإيقاف
                    Box(contentAlignment = Alignment.Center) {
                        if (isBuffering) {
                            CircularProgressIndicator(color = Color(0xFF4CAF50))
                        } else {
                            IconButton(
                                onClick = {
                                    player?.let {
                                        if (it.isPlaying) {
                                            it.pause()
                                        } else {
                                            it.play()
                                        }
                                    }
                                },
                                modifier = Modifier.background(Color(0xFF4CAF50).copy(alpha = 0.8f), RoundedCornerShape(50))
                            ) {
                                Icon(
                                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "إيقاف" else "تشغيل",
                                    tint = Color.White,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }
                    }

                    // تقدم 10 ثواني
                    IconButton(
                        onClick = {
                            player?.let {
                                val newPosition = minOf(it.duration, it.currentPosition + 10000)
                                it.seekTo(newPosition)
                            }
                        },
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            Icons.Default.Forward10,
                            contentDescription = "تقدم 10 ثواني",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // عناصر التحكم السفلية
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    // شريط التقدم
                    Slider(
                        value = if (totalDuration > 0) currentTime.toFloat() / totalDuration.toFloat() else 0f,
                        onValueChange = { progress ->
                            player?.seekTo((progress * totalDuration).toLong())
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF4CAF50),
                            activeTrackColor = Color(0xFF4CAF50),
                            inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // الوقت
                        Row {
                            Text(
                                text = formatTime(currentTime),
                                color = Color.White,
                                fontSize = 12.sp
                            )
                            Text(
                                text = " / ",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 12.sp
                            )
                            Text(
                                text = formatTime(totalDuration),
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                        }

                        // أزرار التحكم الإضافية
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // زر كتم الصوت
                            IconButton(
                                onClick = {
                                    player?.let {
                                        isMuted = !isMuted
                                        it.volume = if (isMuted) 0f else 1f
                                    }
                                },
                                modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            ) {
                                Icon(
                                    if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                    contentDescription = if (isMuted) "إلغاء كتم الصوت" else "كتم الصوت",
                                    tint = Color.White
                                )
                            }

                            // زر سرعة التشغيل
                            Box {
                                IconButton(
                                    onClick = { showSpeedMenu = !showSpeedMenu },
                                    modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                ) {
                                    Text(
                                        text = "${playbackSpeed}x",
                                        color = Color.White,
                                        fontSize = 12.sp
                                    )
                                }

                                // قائمة سرعات التشغيل
                                DropdownMenu(
                                    expanded = showSpeedMenu,
                                    onDismissRequest = { showSpeedMenu = false },
                                    modifier = Modifier.background(Color.Black.copy(alpha = 0.9f))
                                ) {
                                    val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
                                    speeds.forEach { speed ->
                                        DropdownMenuItem(
                                            text = { Text("${speed}x", color = Color.White) },
                                            onClick = {
                                                player?.let {
                                                    playbackSpeed = speed
                                                    it.setPlaybackSpeed(speed)
                                                }
                                                showSpeedMenu = false
                                            }
                                        )
                                    }
                                }
                            }

                            // زر ملء الشاشة
                            IconButton(
                                onClick = { isFullscreen = !isFullscreen },
                                modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            ) {
                                Icon(
                                    if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                    contentDescription = if (isFullscreen) "خروج من ملء الشاشة" else "ملء الشاشة",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // مؤشر التحميل
        if (isBuffering) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF4CAF50))
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
                .background(Color(0xFF4CAF50))
        )
    }
}

private fun formatTime(milliseconds: Long): String {
    val seconds = (milliseconds / 1000).toInt()
    val minutes = seconds / 60
    val hours = minutes / 60
    val remainingSeconds = seconds % 60
    val remainingMinutes = minutes % 60
    
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, remainingMinutes, remainingSeconds)
    } else {
        String.format("%02d:%02d", remainingMinutes, remainingSeconds)
    }
}

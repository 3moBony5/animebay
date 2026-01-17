package io.animebay.stream.ui.screens.details

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.animebay.stream.data.model.Episode
import io.animebay.stream.ui.screens.details.viewmodel.EpisodesViewModel

@Composable
fun EpisodesScreen(
    animeName: String,
    animeUrl: String,
    animeType: String,
    onEpisodeClick: (Episode) -> Unit,
    onNavigateBack: () -> Unit,
    episodesViewModel: EpisodesViewModel = viewModel()
) {
    val uiState by episodesViewModel.uiState.collectAsState()
    val TAG_UI = "Episodes_UI"

    Log.d(TAG_UI, "Recomposing EpisodesScreen. isLoading: ${uiState.isLoading}, error: ${uiState.error}, episodes count: ${uiState.episodes.size}")

    LaunchedEffect(key1 = animeUrl, key2 = animeType) {
        episodesViewModel.fetchAllEpisodes(animeUrl, animeType)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A2332))
            .statusBarsPadding()
    ) {
        TopAppBar(
            title = { 
                Text(
                    text = animeName,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ) 
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "رجوع",
                        tint = Color.White
                    )
                }
            },
            backgroundColor = Color(0xFF1A2332),
            elevation = 0.dp
        )

        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF4CAF50))
                }
            }
            uiState.error != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = uiState.error!!,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = uiState.episodes,
                        key = { episode -> episode.episodeUrl }
                    ) { episode ->
                        EpisodeListItem(
                            episode = episode,
                            onClick = { onEpisodeClick(episode) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EpisodeListItem(
    episode: Episode,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF2A3645))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // رقم الحلقة - Badge مرن
        Box(
            modifier = Modifier
                .widthIn(min = 45.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF37474F))
                .padding(horizontal = 12.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = episode.episodeNumber,
                color = Color(0xFF4CAF50),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                style = LocalTextStyle.current.copy(
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                )
            )
        }
        
        Spacer(modifier = Modifier.width(14.dp))
        
        // النص
        val episodeTitle = if (episode.episodeNumber == "الفلم") {
            "مشاهدة الفلم"
        } else {
            "الحلقة ${episode.episodeNumber}"
        }
        
        Text(
            text = episodeTitle,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
        
        // أيقونة Play
        Icon(
            Icons.Default.PlayArrow,
            contentDescription = "تشغيل",
            tint = Color.White.copy(alpha = 0.5f),
            modifier = Modifier.size(24.dp)
        )
    }
}
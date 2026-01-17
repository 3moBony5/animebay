package io.animebay.stream.ui.screens.servers

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Tv
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.animebay.stream.ui.screens.servers.viewmodel.ServersViewModel
import java.util.UUID

@Composable
fun ServersScreen(
    episodeUrl: String,
    animeName: String,
    episodeNumber: String,
    onNavigateBack: () -> Unit,
    onServerClick: (Server) -> Unit,
    serversViewModel: ServersViewModel = viewModel()
) {
    val uiState by serversViewModel.uiState.collectAsState()

    LaunchedEffect(key1 = episodeUrl) {
        serversViewModel.loadServers(episodeUrl)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A2332))
            .statusBarsPadding()
    ) {
        TopAppBar(
            title = { Text("اختر سيرفر - حلقة $episodeNumber", color = Color.White, fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = Color.White)
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
                    Text(uiState.error!!, color = Color.White)
                }
            }
            else -> {
                ServersList(
                    serversByQuality = uiState.serversByQuality,
                    onServerClick = onServerClick
                )
            }
        }
    }
}

@Composable
private fun ServersList(
    serversByQuality: Map<String, List<Server>>,
    onServerClick: (Server) -> Unit
) {
    val qualityOrder = listOf("1080p - FHD", "720p - HD", "480p - SD", "جودة متعددة")
    val sortedQualities = serversByQuality.keys.sortedBy { qualityOrder.indexOf(it) }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        sortedQualities.forEach { quality ->
            item {
                QualityHeader(quality)
            }
            val servers = serversByQuality[quality] ?: emptyList()
            
            itemsIndexed(
                items = servers,
                key = { _, _ -> UUID.randomUUID().toString() }
            ) { _, server ->
                ServerListItem(server = server, onClick = { onServerClick(server) })
            }
            
            item { 
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun QualityHeader(quality: String) {
    Text(
        text = quality,
        color = Color(0xFF8BC34A),
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
    )
}

@Composable
private fun ServerListItem(
    server: Server,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF2E3B4E).copy(alpha = 0.6f))
            .clickable(onClick = onClick) // رجعنا إلى clickable العادية
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Tv,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = server.name,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

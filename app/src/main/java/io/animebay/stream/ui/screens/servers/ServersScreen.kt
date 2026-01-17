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
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.animebay.stream.data.model.DownloadLink
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
    var showDownloads by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = episodeUrl) {
        serversViewModel.loadServers(episodeUrl)
        serversViewModel.loadDownloadLinks(episodeUrl)
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
            elevation = 0.dp,
            actions = {
                // زر تبديل بين سيرفرات المشاهدة وروابط التحميل
                IconButton(onClick = { showDownloads = !showDownloads }) {
                    Icon(
                        if (showDownloads) Icons.Default.Tv else Icons.Default.Download,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
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
                if (showDownloads) {
                    // عرض روابط التحميل المباشرة
                    DownloadsList(
                        downloadLinks = uiState.downloadLinks,
                        onDownloadClick = { downloadLink ->
                            // فتح الرابط في المتصفح أو تنزيل الملف
                            // يمكنك إضافة منطق التنزيل هنا
                        }
                    )
                } else {
                    // عرض سيرفرات المشاهدة
                    ServersList(
                        serversByQuality = uiState.serversByQuality,
                        onServerClick = onServerClick
                    )
                }
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
private fun DownloadsList(
    downloadLinks: List<DownloadLink>,
    onDownloadClick: (DownloadLink) -> Unit
) {
    // تجميع الروابط حسب الجودة
    val groupedByQuality = downloadLinks.groupBy { it.quality }
    val qualityOrder = listOf("FHD", "HD", "SD", "Unknown")
    val sortedQualities = groupedByQuality.keys.sortedBy { qualityOrder.indexOf(it) }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        sortedQualities.forEach { quality ->
            item {
                QualityHeader("جودة $quality")
            }
            
            val links = groupedByQuality[quality] ?: emptyList()
            
            itemsIndexed(
                items = links,
                key = { _, _ -> UUID.randomUUID().toString() }
            ) { _, downloadLink ->
                DownloadListItem(
                    downloadLink = downloadLink,
                    onClick = { onDownloadClick(downloadLink) }
                )
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
            .clickable(onClick = onClick)
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
        
        Text(
            text = server.quality,
            color = Color(0xFF8BC34A),
            fontSize = 12.sp
        )
    }
}

@Composable
private fun DownloadListItem(
    downloadLink: DownloadLink,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF2E3B4E).copy(alpha = 0.6f))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Download,
                contentDescription = null,
                tint = Color(0xFF8BC34A),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = downloadLink.host,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = downloadLink.url.take(50) + "...",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }
        }
        
        Text(
            text = downloadLink.quality,
            color = Color(0xFF8BC34A),
            fontSize = 12.sp
        )
    }
}

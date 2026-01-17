package io.animebay.stream.ui.screens.home

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.valentinilk.shimmer.shimmer // <-- استيراد مهم
import io.animebay.stream.data.model.Episode
import io.animebay.stream.ui.screens.home.viewmodel.HomeUiState
import io.animebay.stream.ui.screens.home.viewmodel.HomeViewModel
import io.animebay.stream.ui.theme.AnimeBayTheme

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = viewModel(),
    onAnimeClick: (String) -> Unit,
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    Log.d("ViewModelLifecycle", "HomeScreen Composing. ViewModel instance: $homeViewModel")
    
    val uiState by homeViewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        HomeTopBar(
            onSearchClick = onSearchClick,
            onMenuClick = onMenuClick
        )
        
        LatestEpisodesContent(
            uiState = uiState,
            onAnimeClick = onAnimeClick
        )
    }
}

// ▼▼▼ هذا هو الجزء الذي تم تعديله بشكل رئيسي ▼▼▼
@Composable
fun LatestEpisodesContent(
    uiState: HomeUiState,
    modifier: Modifier = Modifier,
    onAnimeClick: (String) -> Unit
) {
    when {
        // الحالة الأولى: التحميل لأول مرة (القائمة فارغة)
        uiState.isLoading && uiState.latestEpisodes.isEmpty() -> {
            LoadingShimmerGrid(modifier) // <-- عرض شاشة التحميل الجديدة
        }
        // الحالة الثانية: لا يوجد بيانات وهناك خطأ
        uiState.latestEpisodes.isEmpty() && !uiState.isLoading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Card(
                    modifier = Modifier.padding(32.dp),
                    backgroundColor = Color(0xFF1a2634).copy(alpha = 0.4f),
                    shape = RoundedCornerShape(16.dp),
                    elevation = 0.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = uiState.error ?: "لم يتم العثور على حلقات.",
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.body1,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
        // الحالة الثالثة: عرض البيانات بنجاح
        else -> {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = modifier.fillMaxSize(), // <-- يأخذ كل المساحة
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = uiState.latestEpisodes,
                    key = { it.episodeUrl },
                    contentType = { "EpisodeCard" }
                ) { episode ->
                    EpisodeCard(
                        animeName = episode.animeName,
                        episodeNumber = episode.episodeNumber,
                        imageUrl = episode.imageUrl,
                        publishedAt = episode.publishedAt,
                        modifier = Modifier,
                        onClick = { onAnimeClick(episode.episodeUrl) }
                    )
                }
            }
        }
    }
}

// ▼▼▼ هذا هو الـ Composable الجديد لشاشة التحميل ▼▼▼
@Composable
private fun LoadingShimmerGrid(modifier: Modifier = Modifier) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier
            .fillMaxSize()
            .shimmer(), // <-- تطبيق تأثير الشيمر هنا
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        userScrollEnabled = false // لا يمكن للمستخدم التمرير أثناء التحميل
    ) {
        items(12) { // عرض 12 بطاقة تحميل وهمية
            ShimmerEpisodeCard()
        }
    }
}

// ▼▼▼ هذه هي بطاقة التحميل الوهمية ▼▼▼
@Composable
private fun ShimmerEpisodeCard(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        // هيكل الصورة
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.7f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Gray.copy(alpha = 0.4f))
        )
        Spacer(modifier = Modifier.height(8.dp))
        // هيكل السطر الأول من النص
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(14.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.Gray.copy(alpha = 0.4f))
        )
        Spacer(modifier = Modifier.height(6.dp))
        // هيكل السطر الثاني من النص
        Box(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(12.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.Gray.copy(alpha = 0.4f))
        )
    }
}


// --- بقية الدوال تبقى كما هي ---
@Composable
fun HomeTopBar(
    onSearchClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = Color(0xFF1a2634).copy(alpha = 0.85f),
        elevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.08f))
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "القائمة",
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(22.dp)
                )
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Anime Bay",
                    style = MaterialTheme.typography.h6.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = Color.White
                )
                Text(
                    text = "أحدث الحلقات",
                    style = MaterialTheme.typography.caption,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp
                )
            }
            
            IconButton(
                onClick = onSearchClick,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.08f))
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "بحث",
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F1419)
@Composable
fun LoadingShimmerGridPreview() {
    AnimeBayTheme(darkTheme = true) {
        LoadingShimmerGrid()
    }
}

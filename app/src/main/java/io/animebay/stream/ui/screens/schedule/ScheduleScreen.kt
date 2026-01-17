package io.animebay.stream.ui.screens.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import io.animebay.stream.data.model.DailySchedule
import io.animebay.stream.data.model.ScheduleAnime
import io.animebay.stream.ui.screens.schedule.viewmodel.ScheduleViewModel
import io.animebay.stream.ui.theme.AppGreen

@Composable
fun ScheduleScreen(
    scheduleViewModel: ScheduleViewModel = viewModel(),
    onAnimeClick: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by scheduleViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1419))
    ) {
        ScheduleTopBar(onBackClick = onBackClick)
        
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = AppGreen,
                            strokeWidth = 3.dp
                        )
                    }
                }
                uiState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.error!!,
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                }
                else -> {
                    ScheduleContent(
                        dailySchedules = uiState.schedule,
                        onAnimeClick = onAnimeClick
                    )
                }
            }
        }
    }
}

@Composable
private fun ScheduleTopBar(onBackClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF1A1F25),
        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(56.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "رجوع",
                    tint = Color.White
                )
            }
            Text(
                text = "جدول الحلقات",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.White,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
private fun ScheduleContent(
    dailySchedules: List<DailySchedule>,
    onAnimeClick: (String) -> Unit
) {
    // 1. استخراج قائمة الأيام
    val days = remember(dailySchedules) { dailySchedules.map { it.day } }
    
    // 2. إنشاء حالة لتتبع اليوم المختار، واختيار أول يوم كقيمة ابتدائية
    var selectedDay by remember { mutableStateOf(days.firstOrNull()) }

    // 3. العثور على بيانات اليوم المختار حالياً
    val selectedSchedule = remember(selectedDay, dailySchedules) {
        dailySchedules.find { it.day == selectedDay }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // 4. عرض شريط الأيام الجديد
        if (days.isNotEmpty() && selectedDay != null) {
            DaysTabs(
                days = days,
                selectedDay = selectedDay!!,
                onDaySelected = { day -> selectedDay = day }
            )
        }
        
        // 5. عرض محتوى اليوم المختار فقط
        if (selectedSchedule != null) {
            DaySection(
                dailySchedule = selectedSchedule,
                onAnimeClick = onAnimeClick
            )
        }
    }
}

// ▼▼▼ هذا هو الـ Composable الجديد لشريط الأيام ▼▼▼
@Composable
private fun DaysTabs(
    days: List<String>,
    selectedDay: String,
    onDaySelected: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(days) { day ->
            val isSelected = day == selectedDay
            val backgroundColor = if (isSelected) AppGreen else Color.Transparent
            val contentColor = if (isSelected) Color(0xFF1A1F25) else Color.White.copy(alpha = 0.7f)

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50)) // شكل بيضاوي
                    .background(backgroundColor)
                    .clickable { onDaySelected(day) }
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day,
                    color = contentColor,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
        }
    }
}


@Composable
private fun DaySection(
    dailySchedule: DailySchedule,
    onAnimeClick: (String) -> Unit
) {
    // تم تبسيط هذا الجزء ليعرض فقط الشبكة
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(), // يأخذ المساحة المتبقية
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(dailySchedule.animes, key = { it.animeUrl }) { anime ->
            AnimeScheduleCard(
                anime = anime,
                onClick = { onAnimeClick(anime.animeUrl) }
            )
        }
    }
}

@Composable
private fun AnimeScheduleCard(
    anime: ScheduleAnime,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        backgroundColor = Color(0xFF1E252E),
        elevation = 2.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(anime.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = anime.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E252E))
                        .padding(6.dp)
                ) {
                    Text(
                        text = anime.name,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

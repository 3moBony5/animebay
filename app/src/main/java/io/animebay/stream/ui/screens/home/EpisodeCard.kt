package io.animebay.stream.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter // <-- ١. استيراد جديد
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import io.animebay.stream.utils.formatTimeAgo

@Composable
fun EpisodeCard(
    animeName: String,
    episodeNumber: String,
    imageUrl: String,
    publishedAt: String?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    // --- بداية التعديل: إنشاء الـ Placeholder برمجياً ---
    // ٢. إنشاء Painter بسيط يرسم لوناً واحداً. هذا أسرع ومقاوم للأخطاء أكثر من ملفات XML.
    val placeholderPainter = remember { ColorPainter(Color(0xFF333942)) }
    // --- نهاية التعديل ---

    Column(
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.7f),
            shape = RoundedCornerShape(12.dp),
            elevation = 0.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = imageUrl,
                        // ٣. استخدام الـ Painter الذي أنشأناه في الأعلى
                        placeholder = placeholderPainter,
                        error = placeholderPainter,
                        contentScale = ContentScale.Crop
                    ),
                    contentDescription = animeName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                if (episodeNumber.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp)
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colors.primary.copy(alpha = 0.9f),
                                        MaterialTheme.colors.primary.copy(alpha = 0.7f)
                                    )
                                ),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "الحلقة $episodeNumber",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = animeName,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        val timeAgo = remember(publishedAt) {
            formatTimeAgo(publishedAt)
        }
        
        if (timeAgo.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = timeAgo,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

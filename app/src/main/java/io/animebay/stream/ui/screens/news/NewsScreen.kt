package io.animebay.stream.ui.screens.news

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

// نموذج بيانات بسيط لتمثيل الخبر
data class NewsItem(
    val title: String,
    val imageUrl: String,
    val link: String
)

@Composable
fun NewsScreen(
    onBackClick: () -> Unit
) {
    // قائمة أخبار وهمية مؤقتًا للتصميم
    val dummyNewsList = listOf(
        NewsItem("عنوان الخبر الأول يظهر هنا", "https://img1.ak.crunchyroll.com/i/spire4/c08c477a3f1b45325cb4354f494e501a1729227731_main.png", "https://www.crunchyroll.com/ar/news"),
        NewsItem("عنوان أطول قليلاً للخبر الثاني لاختبار كيفية ظهوره", "https://img1.ak.crunchyroll.com/i/spire2/08f3484c253d7de11813327a42b172a11729158581_main.jpg", "https://www.crunchyroll.com/ar/news"),
        NewsItem("عنوان خبر ثالث", "https://img1.ak.crunchyroll.com/i/spire1/0e434e3a24e5f20a229c82c3c126593c1729070830_main.jpg", "https://www.crunchyroll.com/ar/news")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("آخر الأخبار") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                backgroundColor = Color(0xFF1a2634),
                contentColor = Color.White
            )
        },
        backgroundColor = Color.Transparent
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(dummyNewsList) { newsItem ->
                NewsCard(newsItem = newsItem)
            }
        }
    }
}

@Composable
fun NewsCard(newsItem: NewsItem) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                // عند الضغط، افتح رابط الخبر في المتصفح
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(newsItem.link))
                context.startActivity(intent)
            },
        shape = RoundedCornerShape(12.dp),
        backgroundColor = Color.White.copy(alpha = 0.05f)
    ) {
        Row(
            modifier = Modifier.height(120.dp)
        ) {
            AsyncImage(
                model = newsItem.imageUrl,
                contentDescription = newsItem.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxHeight()
                    .width(130.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = newsItem.title,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

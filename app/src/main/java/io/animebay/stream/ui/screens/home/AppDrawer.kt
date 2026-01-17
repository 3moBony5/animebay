package io.animebay.stream.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.animebay.stream.R
import io.animebay.stream.ui.theme.AppGreen

@Composable
fun AppDrawer(
    isUserLoggedIn: Boolean,
    userEmail: String?,
    onNavigate: (String) -> Unit,
    onSignOut: () -> Unit
) {
    val currentRoute = "home" // سيتم تعديله لاحقاً

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1a2634),
                        Color(0xFF0a0f14)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 40.dp)
        ) {
            // عرض الواجهة المناسبة بناءً على حالة تسجيل الدخول
            if (isUserLoggedIn) {
                LoggedInDrawerHeader(
                    email = userEmail ?: "لا يوجد بريد إلكتروني",
                    onProfileClick = { onNavigate("profile") }
                )
            } else {
                LoggedOutDrawerHeader(
                    onLoginClick = { onNavigate("login") }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- قسم التصفح ---
            DrawerSection(title = "التصفح") {
                DrawerItem(
                    icon = Icons.Default.Update,
                    text = "اخر التحديثات",
                    isSelected = currentRoute == "home", // Usually the main screen
                    onClick = { onNavigate("home") }
                )
                
                // ▼▼▼ الكود المضاف ▼▼▼
                DrawerItem(
                    icon = Icons.Default.Article,
                    text = "الأخبار",
                    isSelected = currentRoute == "news",
                    onClick = { onNavigate("news") }
                )
                // ▲▲▲ نهاية الكود المضاف ▲▲▲

                DrawerItem(
                    icon = Icons.Default.CalendarToday,
                    text = "جدول الحلقات",
                    isSelected = currentRoute == "schedule",
                    onClick = { onNavigate("schedule") }
                )
                DrawerItem(
                    icon = Icons.AutoMirrored.Filled.List,
                    text = "قائمة الأنمي",
                    isSelected = currentRoute == "anime_list",
                    onClick = { /* لا يفعل شيئاً حالياً */ }
                )
                DrawerItem(
                    icon = Icons.Default.Schedule,
                    text = "قادم قريبا",
                    isSelected = currentRoute == "coming_soon",
                    onClick = { /* لا يفعل شيئاً حالياً */ }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- قسم المكتبة ---
            DrawerSection(title = "مكتبتي") {
                DrawerItem(
                    icon = Icons.Default.Favorite,
                    text = "المفضلة",
                    isSelected = currentRoute == "favorites",
                    onClick = {
                        if (isUserLoggedIn) {
                            onNavigate("favorites")
                        } else {
                            onNavigate("login")
                        }
                    }
                )
                DrawerItem(
                    icon = Icons.Default.History,
                    text = "اخر المشاهدات",
                    isSelected = currentRoute == "history",
                    onClick = { /* لا يفعل شيئاً حالياً */ }
                )
                DrawerItem(
                    icon = Icons.Default.PlaylistPlay,
                    text = "قائمتي",
                    isSelected = currentRoute == "my_list",
                    onClick = { /* لا يفعل شيئاً حالياً */ }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // --- قسم الإعدادات وتسجيل الخروج في الأسفل ---
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Divider(
                    color = Color.White.copy(alpha = 0.08f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                DrawerItem(
                    icon = Icons.Default.Settings,
                    text = "الإعدادات",
                    isSelected = currentRoute == "settings",
                    onClick = { /* لا يفعل شيئاً حالياً */ },
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // ✅ --- إضافة زر تسجيل الخروج --- ✅
                if (isUserLoggedIn) {
                    DrawerItem(
                        icon = Icons.AutoMirrored.Filled.ExitToApp,
                        text = "تسجيل الخروج",
                        isSelected = false,
                        onClick = onSignOut, // استدعاء دالة تسجيل الخروج
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                DrawerFooter()
            }
        }
    }
}

// باقي الدوال في الملف (LoggedInDrawerHeader, LoggedOutDrawerHeader, etc.) تبقى كما هي
@Composable
private fun LoggedInDrawerHeader(
    email: String,
    onProfileClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clickable(onClick = onProfileClick)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "صورة الملف الشخصي",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.08f))
                .border(1.dp, AppGreen, CircleShape)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "مرحباً بك", 
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        
        Text(
            text = email,
            fontSize = 13.sp,
            color = Color.White.copy(alpha = 0.6f),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 4.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun LoggedOutDrawerHeader(onLoginClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clickable(onClick = onLoginClick)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.08f))
                .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "User",
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "تسجيل الدخول",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        
        Text(
            text = "احفظ مفضلاتك وسجلك",
            fontSize = 13.sp,
            color = AppGreen.copy(alpha = 0.9f),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun DrawerSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.5f),
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        content()
    }
}

@Composable
private fun DrawerItem(
    icon: ImageVector,
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) AppGreen.copy(alpha = 0.1f) else Color.Transparent
    val contentColor = if (isSelected) AppGreen else Color.White.copy(alpha = 0.7f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = contentColor,
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = text,
            color = if (isSelected) Color.White else contentColor,
            fontSize = 15.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun DrawerFooter() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "الإصدار 1.0.0",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.05f))
                .clickable { },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "معلومات",
                tint = Color.White.copy(alpha = 0.4f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

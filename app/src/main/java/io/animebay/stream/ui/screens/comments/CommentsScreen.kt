package io.animebay.stream.ui.screens.comments

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import io.animebay.stream.data.model.Comment
import io.animebay.stream.ui.screens.comments.viewmodel.CommentsViewModel
import java.text.SimpleDateFormat
import java.util.*

// الألوان الرئيسية للتطبيق
private val DarkBackground = Color(0xFF0F1419)
private val CardBackground = Color(0xFF1A1F24)
private val NeonGreen = Color(0xFF00E676)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF8899A6)
private val DividerColor = Color(0xFF2F3336)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsScreen(
    animeId: String,
    onNavigateBack: () -> Unit,
    commentsViewModel: CommentsViewModel = viewModel()
) {
    val uiState by commentsViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val currentUserId = Firebase.auth.currentUser?.uid

    LaunchedEffect(key1 = animeId) {
        commentsViewModel.setAnimeId(animeId)
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
            commentsViewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "التعليقات",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground,
                    titleContentColor = TextPrimary
                )
            )
        },
        bottomBar = {
            AddCommentSection(
                onAddComment = { text ->
                    commentsViewModel.addComment(text)
                }
            )
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = NeonGreen,
                    strokeWidth = 3.dp
                )
            } else if (uiState.comments.isEmpty()) {
                EmptyCommentsState(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 12.dp, horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.comments) { comment ->
                        CommentItem(
                            comment = comment,
                            currentUserId = currentUserId,
                            onDelete = { commentId ->
                                commentsViewModel.deleteComment(commentId)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyCommentsState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(CardBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "لا توجد تعليقات بعد",
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "كن أول من يشارك رأيه!",
            color = TextSecondary,
            fontSize = 14.sp
        )
    }
}

@Composable
fun CommentItem(
    comment: Comment,
    currentUserId: String?,
    onDelete: (String) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val isOwnComment = comment.userId == currentUserId

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (comment.userProfilePic == null) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            NeonGreen.copy(alpha = 0.3f),
                                            NeonGreen.copy(alpha = 0.1f)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = NeonGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    } else {
                        AsyncImage(
                            model = comment.userProfilePic,
                            contentDescription = "صورة الملف الشخصي",
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(CardBackground),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = comment.userName,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 15.sp
                        )
                        comment.timestamp?.let {
                            Text(
                                text = formatTimestamp(it),
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                if (isOwnComment) {
                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "خيارات",
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        val customMenuShapes = MaterialTheme.shapes.copy(
                            extraSmall = RoundedCornerShape(12.dp)
                        )
                        val customColorScheme = MaterialTheme.colorScheme.copy(
                            surface = Color(0xFF1F2933),
                            onSurface = TextPrimary
                        )

                        MaterialTheme(
                            colorScheme = customColorScheme,
                            shapes = customMenuShapes
                        ) {
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false },
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "حذف التعليق",
                                            color = Color(0xFFFF6B6B),
                                            fontSize = 14.sp
                                        )
                                    },
                                    onClick = {
                                        comment.id?.let { onDelete(it) }
                                        menuExpanded = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = Color(0xFFFF6B6B),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = comment.text,
                color = TextPrimary.copy(alpha = 0.95f),
                fontSize = 15.sp,
                lineHeight = 22.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCommentSection(onAddComment: (String) -> Unit) {
    var text by remember { mutableStateOf("") }

    Surface(
        color = DarkBackground,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Column {
            Divider(
                color = DividerColor,
                thickness = 1.dp
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            "اكتب تعليقك...",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedTextColor = TextPrimary,
                        focusedTextColor = TextPrimary,
                        unfocusedBorderColor = DividerColor,
                        focusedBorderColor = NeonGreen,
                        cursorColor = NeonGreen,
                        unfocusedContainerColor = CardBackground,
                        focusedContainerColor = CardBackground,
                        unfocusedPlaceholderColor = TextSecondary,
                        focusedPlaceholderColor = TextSecondary
                    ),
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 14.sp
                    )
                )

                Spacer(modifier = Modifier.width(12.dp))

                IconButton(
                    onClick = {
                        if (text.isNotBlank()) {
                            onAddComment(text)
                            text = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    NeonGreen,
                                    Color(0xFF00C853)
                                )
                            ),
                            shape = CircleShape
                        ),
                    enabled = text.isNotBlank()
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "إرسال",
                        tint = DarkBackground,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

private fun formatTimestamp(date: Date): String {
    val seconds = (Date().time - date.time) / 1000
    return when {
        seconds < 60 -> "الآن"
        seconds < 3600 -> "منذ ${seconds / 60} دقيقة"
        seconds < 86400 -> "منذ ${seconds / 3600} ساعة"
        seconds < 604800 -> "منذ ${seconds / 86400} يوم"
        else -> SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
    }
}

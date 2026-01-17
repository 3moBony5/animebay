package io.animebay.stream.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.animebay.stream.ui.screens.home.EpisodeCard
import io.animebay.stream.ui.screens.search.viewmodel.AnimeDetails
import io.animebay.stream.ui.screens.search.viewmodel.SearchUiState
import io.animebay.stream.ui.screens.search.viewmodel.SearchViewModel

@Composable
fun SearchScreen(
    searchViewModel: SearchViewModel = viewModel(),
    onAnimeClick: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by searchViewModel.uiState.collectAsState()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        topBar = {
            SearchTopBar(
                query = uiState.searchQuery,
                onQueryChange = { searchViewModel.onSearchQueryChanged(it) },
                onNavigateBack = onNavigateBack,
                onClearQuery = { searchViewModel.onSearchQueryChanged("") },
                focusRequester = focusRequester
            )
        },
        backgroundColor = Color.Transparent
    ) { paddingValues ->
        SearchContent(
            uiState = uiState,
            modifier = Modifier.padding(paddingValues),
            onAnimeClick = onAnimeClick
        )
    }
}

@Composable
fun SearchTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onClearQuery: () -> Unit,
    focusRequester: FocusRequester
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
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "العودة",
                    tint = Color.White
                )
            }
            TextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                placeholder = {
                    Text(
                        "ابحث عن أنمي...",
                        color = Color.White.copy(alpha = 0.5f)
                    )
                },
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 16.sp
                ),
                singleLine = true,
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent,
                    cursorColor = MaterialTheme.colors.primary,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = onClearQuery) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "مسح البحث",
                                tint = Color.White
                            )
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun SearchContent(
    uiState: SearchUiState,
    modifier: Modifier = Modifier,
    onAnimeClick: (String) -> Unit
) {
    when {
        uiState.isLoading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colors.primary)
            }
        }
        uiState.searchResults.isEmpty() && uiState.hasSearched -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "لا توجد نتائج لهذا البحث.",
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
        uiState.searchResults.isNotEmpty() -> {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = modifier,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = uiState.searchResults,
                    key = { it.episodeUrl }
                ) { anime ->
                    EpisodeCard(
                        animeName = anime.animeName,
                        episodeNumber = anime.episodeNumber,
                        imageUrl = anime.imageUrl,
                        publishedAt = anime.publishedAt,
                        modifier = Modifier,
                        onClick = { onAnimeClick(anime.episodeUrl) }
                    )
                }
                
                // عرض عدد الحلقات إذا كان متاحاً
                item {
                    uiState.episodeCount?.let { count ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            backgroundColor = Color(0xFF4CAF50).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Box(
                                modifier = Modifier.padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$count حلقة متاحة",
                                    color = Color(0xFF8BC34A),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
        else -> {
             Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "ابدأ بالبحث عن الأنمي الذي تحبه.",
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}

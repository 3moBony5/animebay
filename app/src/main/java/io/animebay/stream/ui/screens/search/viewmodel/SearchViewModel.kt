package io.animebay.stream.ui.screens.search.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.animebay.stream.data.model.Episode
import io.animebay.stream.data.repository.AnimeRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// تمت إضافة حقل جديد episodeCount لعرض عدد الحلقات
data class SearchUiState(
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<Episode> = emptyList(),
    val hasSearched: Boolean = false,
    val episodeCount: Int? = null, // عدد الحلقات للأنمي المحدد
    val animeDetails: AnimeDetails? = null // تفاصيل الأنمي المحدد
)

// نموذج بسيط لتفاصيل الأنمي
data class AnimeDetails(
    val name: String,
    val episodeCount: Int,
    val imageUrl: String,
    val description: String?
)

@OptIn(FlowPreview::class)
class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val animeRepository = AnimeRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val searchQueryFlow = MutableStateFlow("")

    init {
        viewModelScope.launch {
            searchQueryFlow
                .debounce(500)
                .distinctUntilChanged()
                .collect { query ->
                    if (query.isBlank()) {
                        _uiState.update { 
                            it.copy(
                                searchResults = emptyList(), 
                                isLoading = false, 
                                hasSearched = false,
                                episodeCount = null,
                                animeDetails = null
                            ) 
                        }
                    } else {
                        _uiState.update { it.copy(isLoading = true, hasSearched = true) }
                        try {
                            val results = animeRepository.searchAnime(query)
                            _uiState.update { 
                                it.copy(
                                    isLoading = false, 
                                    searchResults = results,
                                    episodeCount = null,
                                    animeDetails = null
                                ) 
                            }
                            
                            // إذا كانت هناك نتيجة واحدة فقط، احصل على عدد الحلقات وتفاصيل الأنمي
                            if (results.size == 1) {
                                loadAnimeDetails(results[0])
                            }
                        } catch (e: Exception) {
                            _uiState.update { 
                                it.copy(
                                    isLoading = false, 
                                    searchResults = emptyList(),
                                    episodeCount = null,
                                    animeDetails = null
                                ) 
                            }
                        }
                    }
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchQueryFlow.value = query
    }

    private fun loadAnimeDetails(episode: Episode) {
        viewModelScope.launch {
            try {
                // الحصول على عدد الحلقات
                val episodes = animeRepository.getEpisodes(episode.episodeUrl)
                val episodeCount = episodes.size
                
                // إنشاء تفاصيل الأنمي
                val animeDetails = AnimeDetails(
                    name = episode.animeName,
                    episodeCount = episodeCount,
                    imageUrl = episode.imageUrl,
                    description = null // يمكن إضافته لاحقاً
                )
                
                _uiState.update { 
                    it.copy(
                        episodeCount = episodeCount,
                        animeDetails = animeDetails
                    ) 
                }
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Error loading anime details", e)
            }
        }
    }
}

package io.animebay.stream.ui.screens.search.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.animebay.stream.data.model.Episode
import io.animebay.stream.data.repository.AnimeRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SearchUiState(
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<Episode> = emptyList(),
    // لتتبع ما إذا كان المستخدم قد بحث بالفعل أم لا
    val hasSearched: Boolean = false 
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
                        // امسح النتائج ولكن لا تظهر رسالة "لا توجد نتائج" بعد
                        _uiState.update { it.copy(searchResults = emptyList(), isLoading = false, hasSearched = false) }
                    } else {
                        _uiState.update { it.copy(isLoading = true, hasSearched = true) }
                        try {
                            val results = animeRepository.searchAnime(query)
                            _uiState.update { it.copy(isLoading = false, searchResults = results) }
                        } catch (e: Exception) {
                            _uiState.update { it.copy(isLoading = false, searchResults = emptyList()) }
                        }
                    }
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchQueryFlow.value = query
    }
}

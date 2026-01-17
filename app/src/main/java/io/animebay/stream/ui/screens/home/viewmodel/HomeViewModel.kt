package io.animebay.stream.ui.screens.home.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.animebay.stream.data.model.Episode
import io.animebay.stream.data.repository.AnimeRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// تم تبسيط الحالة لإزالة كل ما يتعلق بالبحث
data class HomeUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val latestEpisodes: List<Episode> = emptyList()
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val animeRepository = AnimeRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            delay(50L) 
            loadLatestEpisodes()
        }
    }

    // تم حذف كل شيء يتعلق بالبحث من هنا

    private fun loadLatestEpisodes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val episodes = animeRepository.getLatestEpisodes(1)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        latestEpisodes = episodes,
                        error = if (episodes.isEmpty()) "فشل تحميل الحلقات. تحقق من اتصالك بالإنترنت." else null
                    )
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error loading latest episodes", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "حدث خطأ ما. يرجى المحاولة مرة أخرى."
                    )
                }
            }
        }
    }
}

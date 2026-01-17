package io.animebay.stream.ui.screens.details.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.animebay.stream.data.model.Episode
import io.animebay.stream.data.repository.AnimeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EpisodesUiState(
    val isLoading: Boolean = true,
    val episodes: List<Episode> = emptyList(),
    val error: String? = null
)

class EpisodesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AnimeRepository(application)

    private val _uiState = MutableStateFlow(EpisodesUiState())
    val uiState = _uiState.asStateFlow()

    // ✅✅✅ --- بداية التعديل --- ✅✅✅
    // تم تعديل الدالة لتستقبل النوع أيضًا
    fun fetchAllEpisodes(animeUrl: String, animeType: String) {
        val TAG_VM = "Episodes_VM"
        Log.d(TAG_VM, "--- ViewModel received URL: $animeUrl, Type: $animeType ---")

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) } // إعادة تعيين الخطأ
            
            // نمرر الرابط والنوع مباشرة إلى Repository
            val result = repository.getAllEpisodesFromEpisodePage(animeUrl, animeType)

            Log.d(TAG_VM, "Repository returned ${result.size} episodes.")

            if (result.isNotEmpty()) {
                _uiState.update { it.copy(isLoading = false, episodes = result) }
                Log.d(TAG_VM, "State updated with SUCCESS.")
            } else {
                _uiState.update { it.copy(isLoading = false, error = "فشل جلب قائمة الحلقات.") }
                Log.e(TAG_VM, "State updated with ERROR because result is empty.")
            }
        }
    }
    // ✅✅✅ --- نهاية التعديل --- ✅✅✅
}

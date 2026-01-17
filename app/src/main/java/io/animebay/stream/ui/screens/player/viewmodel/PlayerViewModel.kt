package io.animebay.stream.ui.screens.player.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PlayerUiState(
    val isLoading: Boolean = true,
    val videoUrl: String? = null,
    val error: String? = null,
)

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState = _uiState.asStateFlow()

    fun setVideoUrl(url: String) {
        if (_uiState.value.videoUrl == null) { // لمنع التحديث المتكرر
            _uiState.update { it.copy(isLoading = false, videoUrl = url, error = null) }
        }
    }

    fun setError(message: String) {
        if (_uiState.value.videoUrl == null) { // لا تعرض خطأ إذا نجحنا بالفعل
            _uiState.update { it.copy(isLoading = false, error = message) }
        }
    }
}

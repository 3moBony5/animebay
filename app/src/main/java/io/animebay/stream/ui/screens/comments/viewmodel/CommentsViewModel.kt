package io.animebay.stream.ui.screens.comments.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.animebay.stream.data.model.Comment
import io.animebay.stream.data.repository.CommentsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CommentsUiState(
    val comments: List<Comment> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class CommentsViewModel : ViewModel() {
    private val commentsRepository = CommentsRepository()

    private val _uiState = MutableStateFlow(CommentsUiState())
    val uiState = _uiState.asStateFlow()

    private var currentAnimeId: String? = null

    fun setAnimeId(animeId: String) {
        // نستدعي دالة الجلب فقط في المرة الأولى لتجنب التكرار عند إعادة بناء الواجهة
        if (currentAnimeId == null) {
            currentAnimeId = animeId
            fetchComments()
        }
    }

    private fun fetchComments() {
        val animeId = currentAnimeId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val commentsList = commentsRepository.getComments(animeId)
            _uiState.update {
                it.copy(
                    comments = commentsList,
                    isLoading = false
                )
            }
        }
    }

    fun addComment(text: String) {
        val animeId = currentAnimeId ?: return
        viewModelScope.launch {
            val success = commentsRepository.addComment(animeId, text)
            if (success) {
                // بعد الإضافة الناجحة، نعيد جلب القائمة المحدثة
                fetchComments()
            } else {
                _uiState.update { it.copy(error = "فشل إرسال التعليق. حاول مرة أخرى.") }
            }
        }
    }

    fun deleteComment(commentId: String) {
        viewModelScope.launch {
            val success = commentsRepository.deleteComment(commentId)
            if (success) {
                // بعد الحذف الناجح، نعيد جلب القائمة المحدثة
                fetchComments()
            } else {
                _uiState.update { it.copy(error = "فشل حذف التعليق.") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

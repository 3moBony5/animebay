// المسار: ui/screens/details/viewmodel/DetailsViewModel.kt

package io.animebay.stream.ui.screens.details.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.animebay.stream.data.model.AnimeDetails
import io.animebay.stream.data.model.Comment // <-- 1. استيراد نموذج التعليق
import io.animebay.stream.data.repository.AnimeRepository
import io.animebay.stream.data.repository.CommentsRepository // <-- 2. استيراد الريبو الجديد
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// --- 3. تحديث UiState ليحتوي على التعليقات ---
data class DetailsUiState(
    val isLoading: Boolean = true,
    val animeDetails: AnimeDetails? = null,
    val error: String? = null,
    
    // -- الإضافات الجديدة --
    val comments: List<Comment> = emptyList(), // قائمة لتخزين التعليقات
    val isLoadingComments: Boolean = false,    // لتتبع حالة تحميل التعليقات
    val commentError: String? = null           // لتخزين أي خطأ يتعلق بالتعليقات
)

class DetailsViewModel(application: Application) : AndroidViewModel(application) {
    private val animeRepository = AnimeRepository(application)
    private val commentsRepository = CommentsRepository() // <-- 4. إنشاء نسخة من ريبو التعليقات

    private val _uiState = MutableStateFlow(DetailsUiState())
    val uiState = _uiState.asStateFlow()

    // هذه الدالة تبقى كما هي
    fun getAnimeDetails(animeUrl: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = animeRepository.getAnimeDetails(animeUrl)
            if (result != null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        animeDetails = result
                    )
                }
                // بعد جلب تفاصيل الأنمي بنجاح، نقوم بجلب التعليقات الخاصة به
                fetchComments(animeUrl) // <-- 5. استدعاء دالة جلب التعليقات
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "فشل جلب تفاصيل الأنمي."
                    )
                }
            }
        }
    }
    
    // --- 6. إضافة الدوال الجديدة الخاصة بالتعليقات ---

    /**
     * دالة لجلب التعليقات من الريبو وتحديث الـ UiState.
     */
    private fun fetchComments(animeId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingComments = true) } // بدأ تحميل التعليقات
            val commentsList = commentsRepository.getComments(animeId)
            _uiState.update {
                it.copy(
                    comments = commentsList,
                    isLoadingComments = false // انتهى تحميل التعليقات
                )
            }
        }
    }

    /**
     * دالة لإضافة تعليق جديد.
     */
    fun addComment(animeId: String, text: String) {
        // نتأكد أن نص التعليق ليس فارغاً
        if (text.isBlank()) {
            _uiState.update { it.copy(commentError = "لا يمكن إرسال تعليق فارغ.") }
            return
        }

        viewModelScope.launch {
            val success = commentsRepository.addComment(animeId, text)
            if (success) {
                // إذا نجحت الإضافة، نعيد جلب التعليقات لتحديث القائمة فوراً
                fetchComments(animeId)
            } else {
                // إذا فشلت، نُظهر رسالة خطأ
                _uiState.update { it.copy(commentError = "فشل إرسال التعليق. حاول مرة أخرى.") }
            }
        }
    }

    /**
     * دالة لمسح رسالة الخطأ بعد عرضها للمستخدم.
     */
    fun clearCommentError() {
        _uiState.update { it.copy(commentError = null) }
    }
}

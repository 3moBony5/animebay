package io.animebay.stream.ui.screens.schedule.viewmodel // <-- تم تعديل الحزمة لتعكس المجلد الجديد

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.animebay.stream.data.model.DailySchedule
import io.animebay.stream.data.repository.AnimeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ScheduleUiState(
    val isLoading: Boolean = true,
    val schedule: List<DailySchedule> = emptyList(),
    val error: String? = null
)

class ScheduleViewModel(application: Application) : AndroidViewModel(application) {

    private val animeRepository = AnimeRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    init {
        loadSchedule()
    }

    fun loadSchedule() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val scheduleData = animeRepository.getAnimeSchedule()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        schedule = scheduleData,
                        error = if (scheduleData.isEmpty()) "فشل تحميل جدول الحلقات. تحقق من اتصالك بالإنترنت." else null
                    )
                }
            } catch (e: Exception) {
                Log.e("ScheduleViewModel", "Error loading anime schedule", e)
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

package io.animebay.stream.ui.screens.servers.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.animebay.stream.data.repository.AnimeRepository
import io.animebay.stream.ui.screens.servers.Server
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ServersUiState(
    val isLoading: Boolean = true,
    val serversByQuality: Map<String, List<Server>> = emptyMap(),
    val error: String? = null
)

class ServersViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AnimeRepository(application)

    private val _uiState = MutableStateFlow(ServersUiState())
    val uiState = _uiState.asStateFlow()

    private fun getDisplayName(originalName: String): String {
        return when {
            originalName.contains("yonaplay", ignoreCase = true) -> "YP"
            originalName.contains("ok.ru", ignoreCase = true) -> "OK"
            originalName.contains("videa", ignoreCase = true) -> "VD"
            originalName.contains("streamwish", ignoreCase = true) -> "SW"
            originalName.contains("dailymotion", ignoreCase = true) -> "DM"
            originalName.contains("mediafire", ignoreCase = true) -> "MF"
            originalName.contains("workupload", ignoreCase = true) -> "WU"
            originalName.contains("hexload", ignoreCase = true) -> "HL"
            originalName.contains("gofile", ignoreCase = true) -> "GF"
            else -> originalName.trim()
        }
    }

    private fun getQualityFromName(name: String): String {
        return when {
            name.contains("FHD", ignoreCase = true) -> "1080p - FHD"
            name.contains("HD", ignoreCase = true) -> "720p - HD"
            name.contains("SD", ignoreCase = true) -> "480p - SD"
            else -> "جودة متعددة"
        }
    }

    // --- تم تعديل هذه الدالة بالكامل ---
    fun loadServers(episodeUrl: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // استدعاء الدالة الجديدة من الـ Repository
                val serversData = repository.getEpisodeServers(episodeUrl)

                if (serversData.isEmpty()) {
                    Log.w("ServersViewModel", "No servers found for URL: $episodeUrl")
                    _uiState.update { it.copy(isLoading = false, error = "لم يتم العثور على سيرفرات.") }
                } else {
                    // تحويل قائمة الأزواج (Pair) إلى قائمة كائنات (Server)
                    val servers = serversData.map { (name, embedUrl) ->
                        Server(
                            name = getDisplayName(name),
                            embedUrl = embedUrl,
                            quality = getQualityFromName(name)
                        )
                    }
                    // تجميع السيرفرات حسب الجودة
                    val groupedServers = servers.groupBy { it.quality }
                    _uiState.update { it.copy(isLoading = false, serversByQuality = groupedServers) }
                }
            } catch (e: Exception) {
                Log.e("ServersViewModel", "Error loading servers for $episodeUrl", e)
                _uiState.update { it.copy(isLoading = false, error = "حدث خطأ أثناء جلب السيرفرات.") }
            }
        }
    }
}

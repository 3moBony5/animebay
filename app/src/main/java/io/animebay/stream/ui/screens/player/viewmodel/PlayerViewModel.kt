package io.animebay.stream.ui.screens.player.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.animebay.stream.data.model.Server
import io.animebay.stream.data.repository.AnimeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

data class PlayerUiState(
    val isLoading: Boolean = true,
    val videoUrl: String? = null,
    val servers: List<Server> = emptyList(),
    val currentServerIndex: Int = 0,
    val error: String? = null,
    val episodeTitle: String = "",
    val isBuffering: Boolean = false,
    val playbackSpeed: Float = 1.0f,
    val isMuted: Boolean = false,
    val videoQuality: String = "Auto",
    val availableQualities: List<String> = emptyList()
)

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AnimeRepository(application)
    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState = _uiState.asStateFlow()

    fun setVideoUrl(url: String) {
        if (_uiState.value.videoUrl == null) {
            _uiState.update { it.copy(isLoading = false, videoUrl = url, error = null) }
        }
    }

    fun setError(message: String) {
        if (_uiState.value.videoUrl == null) {
            _uiState.update { it.copy(isLoading = false, error = message) }
        }
    }

    fun setEpisodeInfo(title: String, servers: List<Server>) {
        _uiState.update { 
            it.copy(
                episodeTitle = title,
                servers = servers,
                isLoading = false
            )
        }
    }

    fun switchServer(index: Int) {
        if (index >= 0 && index < _uiState.value.servers.size) {
            val newServer = _uiState.value.servers[index]
            _uiState.update { 
                it.copy(
                    videoUrl = newServer.embedUrl,
                    currentServerIndex = index,
                    isLoading = true
                )
            }
        }
    }

    fun setBuffering(isBuffering: Boolean) {
        _uiState.update { it.copy(isBuffering = isBuffering) }
    }

    fun setPlaybackSpeed(speed: Float) {
        _uiState.update { it.copy(playbackSpeed = speed) }
    }

    fun setMuted(isMuted: Boolean) {
        _uiState.update { it.copy(isMuted = isMuted) }
    }

    fun setVideoQuality(quality: String) {
        _uiState.update { it.copy(videoQuality = quality) }
    }

    // دالة لمحاولة السيرفر التالي عند فشل التشغيل
    fun tryNextServer(): Boolean {
        val currentIndex = _uiState.value.currentServerIndex
        if (currentIndex < _uiState.value.servers.size - 1) {
            switchServer(currentIndex + 1)
            return true
        }
        return false
    }

    // دالة لتحويل روابط السيرفرات المختلفة إلى روابط مباشرة
    suspend fun resolveServerUrl(serverUrl: String): String {
        return try {
            // محاولة استخراج الرابط المباشر من السيرفر
            when {
                // YonaPlay
                serverUrl.contains("yonaplay", ignoreCase = true) -> {
                    resolveYonaPlayUrl(serverUrl)
                }
                
                // StreamWish
                serverUrl.contains("streamwish", ignoreCase = true) -> {
                    resolveStreamWishUrl(serverUrl)
                }
                
                // Videa
                serverUrl.contains("videa", ignoreCase = true) -> {
                    resolveVideaUrl(serverUrl)
                }
                
                // Dailymotion
                serverUrl.contains("dailymotion", ignoreCase = true) -> {
                    resolveDailymotionUrl(serverUrl)
                }
                
                // VidBom
                serverUrl.contains("vidbom", ignoreCase = true) -> {
                    resolveVidBomUrl(serverUrl)
                }
                
                // Uqload
                serverUrl.contains("uqload", ignoreCase = true) -> {
                    resolveUqloadUrl(serverUrl)
                }
                
                // روابط مباشرة
                serverUrl.endsWith(".m3u8", ignoreCase = true) || 
                serverUrl.endsWith(".mp4", ignoreCase = true) -> {
                    serverUrl
                }
                
                // افتراضياً إرجاع نفس الرابط
                else -> serverUrl
            }
        } catch (e: Exception) {
            Log.e("PlayerViewModel", "Error resolving server URL: $serverUrl", e)
            serverUrl
        }
    }

    private suspend fun resolveYonaPlayUrl(serverUrl: String): String {
        return withContext(Dispatchers.IO) {
            try {
                // محاولة استخراج m3u8 من YonaPlay
                // هذا مثال مبسط - يمكن توسيعه لاحقاً
                Log.d("PlayerViewModel", "Resolving YonaPlay URL: $serverUrl")
                serverUrl
            } catch (e: Exception) {
                Log.e("PlayerViewModel", "Error resolving YonaPlay URL", e)
                serverUrl
            }
        }
    }

    private suspend fun resolveStreamWishUrl(serverUrl: String): String {
        return withContext(Dispatchers.IO) {
            try {
                // محاولة استخراج m3u8 من StreamWish
                Log.d("PlayerViewModel", "Resolving StreamWish URL: $serverUrl")
                serverUrl
            } catch (e: Exception) {
                Log.e("PlayerViewModel", "Error resolving StreamWish URL", e)
                serverUrl
            }
        }
    }

    private suspend fun resolveVideaUrl(serverUrl: String): String {
        return withContext(Dispatchers.IO) {
            try {
                // محاولة استخراج m3u8 من Videa
                Log.d("PlayerViewModel", "Resolving Videa URL: $serverUrl")
                serverUrl
            } catch (e: Exception) {
                Log.e("PlayerViewModel", "Error resolving Videa URL", e)
                serverUrl
            }
        }
    }

    private suspend fun resolveDailymotionUrl(serverUrl: String): String {
        return withContext(Dispatchers.IO) {
            try {
                // محاولة استخراج m3u8 من Dailymotion
                Log.d("PlayerViewModel", "Resolving Dailymotion URL: $serverUrl")
                serverUrl
            } catch (e: Exception) {
                Log.e("PlayerViewModel", "Error resolving Dailymotion URL", e)
                serverUrl
            }
        }
    }

    private suspend fun resolveVidBomUrl(serverUrl: String): String {
        return withContext(Dispatchers.IO) {
            try {
                // محاولة استخراج m3u8 من VidBom
                Log.d("PlayerViewModel", "Resolving VidBom URL: $serverUrl")
                serverUrl
            } catch (e: Exception) {
                Log.e("PlayerViewModel", "Error resolving VidBom URL", e)
                serverUrl
            }
        }
    }

    private suspend fun resolveUqloadUrl(serverUrl: String): String {
        return withContext(Dispatchers.IO) {
            try {
                // محاولة استخراج m3u8 من Uqload
                Log.d("PlayerViewModel", "Resolving Uqload URL: $serverUrl")
                serverUrl
            } catch (e: Exception) {
                Log.e("PlayerViewModel", "Error resolving Uqload URL", e)
                serverUrl
            }
        }
    }

    // دالة للحصول على السيرفرات المتاحة لحلقة معينة
    fun getServersForEpisode(episodeUrl: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                val servers = repository.getEpisodeServers(episodeUrl)
                _uiState.update { 
                    it.copy(
                        servers = servers,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                Log.e("PlayerViewModel", "Error getting servers", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "فشل في الحصول على السيرفرات"
                    )
                }
            }
        }
    }

    // دالة للحصول على روابط التحميل
    fun getDownloadLinks(episodeUrl: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                val downloadLinks = repository.getDownloadLinks(episodeUrl)
                // يمكن معالجة روابط التحميل هنا
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                Log.e("PlayerViewModel", "Error getting download links", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "فشل في الحصول على روابط التحميل"
                    )
                }
            }
        }
    }

    // دالة لحفظ آخر وقت مشاهدة
    fun saveWatchProgress(animeId: String, episodeId: String, progress: Long) {
        viewModelScope.launch {
            try {
                val prefs = getApplication<Application>().getSharedPreferences("watch_progress", Context.MODE_PRIVATE)
                prefs.edit().putLong("${animeId}_$episodeId", progress).apply()
            } catch (e: Exception) {
                Log.e("PlayerViewModel", "Error saving watch progress", e)
            }
        }
    }

    // دالة لاستعادة آخر وقت مشاهدة
    fun getWatchProgress(animeId: String, episodeId: String): Long {
        return try {
            val prefs = getApplication<Application>().getSharedPreferences("watch_progress", Context.MODE_PRIVATE)
            prefs.getLong("${animeId}_$episodeId", 0L)
        } catch (e: Exception) {
            Log.e("PlayerViewModel", "Error getting watch progress", e)
            0L
        }
    }
}

package io.animebay.stream.data.model

// لا حاجة لعمل import هنا

data class AnimeDetails(
    val name: String = "جاري التحميل...",
    val imageUrl: String = "",
    val story: String = "...",
    val genres: List<String> = emptyList(),
    val rating: String = "...",
    val episodeDuration: String = "...",
    val source: String = "...",
    val type: String = "...",
    val status: String = "...",
    // ✅ --- أضف هذا السطر فقط --- ✅
    val latestEpisodePublishedAt: String? = null 
)

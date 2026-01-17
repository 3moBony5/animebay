package io.animebay.stream.data.model

// هذا الكلاس يمثل حلقة أنمي واحدة
data class Episode(
    val animeName: String,
    val episodeNumber: String,
    val imageUrl: String,
    val episodeUrl: String,
    val source: String, // اسم الموقع الذي جُلبت منه الحلقة (witanime أو anime3rb)
    val publishedAt: String? = null // <-- هذا هو الحقل الجديد لتاريخ النشر
)

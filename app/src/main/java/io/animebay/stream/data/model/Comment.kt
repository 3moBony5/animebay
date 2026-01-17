// المسار: data/model/Comment.kt

package io.animebay.stream.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * هذا الكلاس يمثل شكل التعليق الواحد في قاعدة البيانات وفي التطبيق.
 */
data class Comment(
    // معلومات أساسية للتعليق
    val text: String = "",
    val animeId: String = "",

    // معلومات المستخدم الذي كتب التعليق
    val userId: String = "",
    val userName: String = "مستخدم",
    val userProfilePic: String? = null, // قد لا يمتلك المستخدم صورة

    // معلومات إضافية
    @ServerTimestamp // فيرستور سيقوم بملء هذا الحقل تلقائياً بوقت الخادم
    val timestamp: Date? = null,
    
    // هذا الحقل لن يتم حفظه في فيرستور، سنستخدمه داخل التطبيق فقط
    val id: String = "" 
)

package io.animebay.stream.ui.screens.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// لم يتغير
data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val bio: String = "",
    val profileImageUrl: String = ""
)

// ✅ تم إضافة حالة جديدة `isUploadingImage`
data class ProfileUiState(
    val isLoading: Boolean = true,
    val userProfile: UserProfile? = null,
    val error: String? = null,
    val isUpdating: Boolean = false,
    val isUploadingImage: Boolean = false // حالة خاصة لعملية رفع الصورة
)

class ProfileViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val currentUser = Firebase.auth.currentUser

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    // ✅ --- دالة جديدة لإعداد Cloudinary ---
    // يجب استدعاؤها مرة واحدة فقط عند بدء التطبيق
    companion object {
        private var isCloudinaryConfigured = false
        fun configureCloudinary(context: Context) {
            if (isCloudinaryConfigured) return
            try {
                val config = mapOf(
                    "cloud_name" to "dnoadv3zr",
                    "api_key" to "163636711911787",
                    "api_secret" to "9kArQAAmhnx7hQvM5bPHCbnj0u0"
                )
                MediaManager.init(context, config)
                isCloudinaryConfigured = true
            } catch (e: Exception) {
                // يمكنك تسجيل الخطأ هنا إذا أردت
            }
        }
    }

    private fun loadUserProfile() {
        if (currentUser == null) {
            _uiState.update { it.copy(isLoading = false, error = "المستخدم غير مسجل دخوله.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val document = db.collection("users").document(currentUser.uid).get().await()
                if (document.exists()) {
                    val userProfile = document.toObject(UserProfile::class.java)?.copy(
                        uid = currentUser.uid,
                        // تأكد من أن البريد الإلكتروني مأخوذ من المصادقة لضمان صحته
                        email = currentUser.email ?: ""
                    )
                    _uiState.update { it.copy(isLoading = false, userProfile = userProfile) }
                } else {
                     _uiState.update { it.copy(isLoading = false, error = "لم يتم العثور على ملف المستخدم.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun updateName(newName: String) {
        if (currentUser == null || newName.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true) }
            try {
                db.collection("users").document(currentUser.uid).update("name", newName).await()
                _uiState.update {
                    it.copy(isUpdating = false, userProfile = it.userProfile?.copy(name = newName))
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isUpdating = false) }
            }
        }
    }

    fun updateBio(newBio: String) {
        if (currentUser == null) return
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true) }
            try {
                db.collection("users").document(currentUser.uid).update("bio", newBio).await()
                _uiState.update {
                    it.copy(isUpdating = false, userProfile = it.userProfile?.copy(bio = newBio))
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isUpdating = false) }
            }
        }
    }

    // ✅ --- الدالة الأهم: لرفع الصورة ---
    fun uploadProfileImage(uri: Uri) {
        if (currentUser == null) return
        
        _uiState.update { it.copy(isUploadingImage = true) }

        MediaManager.get().upload(uri)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {
                    // بدأ الرفع
                }

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                    // يمكنك استخدام هذا لتحديث شريط تقدم إذا أردت
                }

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    // --- نجح الرفع! ---
                    val imageUrl = resultData["secure_url"] as? String
                    if (imageUrl != null) {
                        // الآن، احفظ رابط الصورة في Firestore
                        viewModelScope.launch {
                            try {
                                db.collection("users").document(currentUser.uid)
                                    .update("profileImageUrl", imageUrl)
                                    .await()
                                
                                // تحديث الواجهة بالصورة الجديدة فورًا
                                _uiState.update {
                                    it.copy(
                                        isUploadingImage = false,
                                        userProfile = it.userProfile?.copy(profileImageUrl = imageUrl)
                                    )
                                }
                            } catch (e: Exception) {
                                // فشل تحديث Firestore
                                _uiState.update { it.copy(isUploadingImage = false, error = "فشل حفظ رابط الصورة.") }
                            }
                        }
                    } else {
                         _uiState.update { it.copy(isUploadingImage = false, error = "لم يتم العثور على رابط الصورة بعد الرفع.") }
                    }
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    // فشل الرفع إلى Cloudinary
                    _uiState.update { it.copy(isUploadingImage = false, error = "فشل رفع الصورة: ${error.description}") }
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {
                    // تمت إعادة جدولة الرفع
                }
            }).dispatch()
    }
}

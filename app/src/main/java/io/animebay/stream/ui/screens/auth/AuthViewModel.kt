package io.animebay.stream.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// ✅ --- الخطوة 1: تحديث AuthUiState ---
data class AuthUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null, // تمت إضافة هذا السطر
    val isLoggedIn: Boolean = false,
    val userEmail: String? = null,
    val authCheckCompleted: Boolean = false
)

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth
    private val db = Firebase.firestore

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _uiState.update {
                it.copy(
                    isLoggedIn = user != null,
                    userEmail = user?.email,
                    authCheckCompleted = true
                )
            }
        }
    }

    // ✅ --- الخطوة 2: إضافة الدالة الجديدة ---
    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            if (email.isBlank()) {
                _uiState.update { it.copy(error = "الرجاء إدخال البريد الإلكتروني.") }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            try {
                auth.sendPasswordResetEmail(email).await()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "تم إرسال رابط إعادة التعيين إلى بريدك بنجاح."
                    )
                }
            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("USER_NOT_FOUND") == true -> "هذا البريد الإلكتروني غير مسجل."
                    else -> e.message ?: "فشل إرسال الرابط."
                }
                _uiState.update { it.copy(isLoading = false, error = errorMessage) }
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, isSuccess = false) }
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(credential).await()
                val user = authResult.user

                val isNewUser = authResult.additionalUserInfo?.isNewUser ?: false

                if (user != null && isNewUser) {
                    val userProfile = hashMapOf(
                        "name" to user.displayName,
                        "email" to user.email,
                        "bio" to "",
                        "profileImageUrl" to (user.photoUrl?.toString() ?: "")
                    )
                    db.collection("users").document(user.uid).set(userProfile).await()
                }
                
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "فشل تسجيل الدخول عبر جوجل.") }
            }
        }
    }

    fun signOut() {
        auth.signOut()
    }

    fun signUp(name: String, email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, isSuccess = false) }
            try {
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val user = authResult.user

                if (user != null) {
                    val userProfile = hashMapOf(
                        "name" to name,
                        "email" to email,
                        "bio" to "",
                        "profileImageUrl" to ""
                    )
                    db.collection("users").document(user.uid).set(userProfile).await()
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "فشل إنشاء المستخدم.") }
                }
            } catch (e: FirebaseAuthUserCollisionException) {
                _uiState.update { it.copy(isLoading = false, error = "هذا البريد الإلكتروني مستخدم بالفعل.") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "حدث خطأ غير متوقع.") }
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, isSuccess = false) }
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "البريد الإلكتروني أو كلمة المرور غير صحيحة.") }
            }
        }
    }

    // ✅ --- الخطوة 3: تحديث دالة resetState ---
    fun resetState() {
        _uiState.update { it.copy(isLoading = false, isSuccess = false, error = null, successMessage = null) }
    }
}

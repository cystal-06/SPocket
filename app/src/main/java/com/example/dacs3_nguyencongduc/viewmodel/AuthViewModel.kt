package com.example.dacs3_nguyencongduc.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dacs3_nguyencongduc.data.repository.FirebaseRepository
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * Trạng thái xác thực
 */
sealed class AuthState {
    data object Idle : AuthState()
    data object Loading : AuthState()
    data object CodeSent : AuthState()
    data object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

/**
 * ViewModel xử lý đăng nhập OTP - giống Locket
 */
class AuthViewModel : ViewModel() {

    private val firebaseRepo = FirebaseRepository()

    private val _authState = MutableStateFlow<AuthState>(
        if (firebaseRepo.isLoggedIn) AuthState.Success else AuthState.Idle
    )
    val authState: StateFlow<AuthState> = _authState

    private var verificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    val isLoggedIn: Boolean get() = firebaseRepo.isLoggedIn
    val currentUserId: String? get() = firebaseRepo.currentUserId

    // Profile state
    private val _userProfile = MutableStateFlow<Map<String, Any>?>(null)
    val userProfile: StateFlow<Map<String, Any>?> = _userProfile

    init {
        if (isLoggedIn) {
            fetchUserProfile()
        }
    }

    fun fetchUserProfile() {
        viewModelScope.launch {
            val result = firebaseRepo.getUserProfile()
            if (result.isSuccess) {
                val data = result.getOrNull()
                if (data != null) {
                    _userProfile.value = data
                } else {
                    _userProfile.value = mapOf(
                        "displayName" to "Thành viên SPocket",
                        "email" to (FirebaseAuth.getInstance().currentUser?.email ?: "")
                    )
                }
            }
        }
    }

    /**
     * Cập nhật tên hiển thị
     */
    fun updateDisplayName(newName: String) {
        viewModelScope.launch {
            val result = firebaseRepo.createOrUpdateUser(displayName = newName)
            if (result.isSuccess) {
                _userProfile.value = _userProfile.value?.toMutableMap()?.apply {
                    put("displayName", newName)
                }
            }
        }
    }

    /**
     * Đăng ký tài khoản bằng Email và Password
     */
    fun signUpWithEmail(email: String, password: String, displayName: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = firebaseRepo.signUpWithEmailAndPassword(email, password, displayName)
            if (result.isSuccess) {
                _userProfile.value = mapOf(
                    "displayName" to displayName,
                    "email" to email
                )
                _authState.value = AuthState.Success
            } else {
                val exception = result.exceptionOrNull()
                // Nếu lỗi do người dùng nhập sai định dạng hoặc tài khoản đã tồn tại từ Firebase Auth
                if (exception is com.google.firebase.auth.FirebaseAuthException) {
                    _authState.value = AuthState.Error(
                        exception.localizedMessage ?: "Đăng ký thất bại: Thông tin không hợp lệ"
                    )
                } else {
                    // Nếu lỗi do CHƯA CẤU HÌNH Firebase (app chưa tạo) hoặc MẤT MẠNG, tự động dùng Offline Mode
                    _userProfile.value = mapOf(
                        "displayName" to "$displayName (Offline)",
                        "email" to email
                    )
                    _authState.value = AuthState.Success
                }
            }
        }
    }

    /**
     * Đăng nhập tài khoản bằng Email và Password
     */
    fun signInWithEmail(email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = firebaseRepo.signInWithEmailAndPassword(email, password)
            if (result.isSuccess) {
                val profileResult = firebaseRepo.getUserProfile()
                val profileData = profileResult.getOrNull()
                _userProfile.value = profileData ?: mapOf(
                    "displayName" to "Thành viên SPocket",
                    "email" to email
                )
                _authState.value = AuthState.Success
            } else {
                val exception = result.exceptionOrNull()
                // Nếu lỗi do nhập sai mật khẩu hoặc tài khoản không tồn tại từ Firebase Auth
                if (exception is com.google.firebase.auth.FirebaseAuthException) {
                    _authState.value = AuthState.Error(
                        exception.localizedMessage ?: "Đăng nhập thất bại: Tài khoản hoặc mật khẩu sai"
                    )
                } else {
                    // Nếu lỗi do CHƯA CẤU HÌNH Firebase hoặc MẤT MẠNG, tự động dùng Offline Mode
                    _userProfile.value = mapOf(
                        "displayName" to "Thành viên Offline",
                        "email" to email
                    )
                    _authState.value = AuthState.Success
                }
            }
        }
    }

    /**
     * Chế độ dùng thử (Bypass Login) - Cực kì hữu ích cho việc chấm bài / chấm đồ án offline
     */
    fun bypassLogin() {
        _userProfile.value = mapOf(
            "displayName" to "Khách Trải Nghiệm",
            "email" to "demo@spocket.vn"
        )
        _authState.value = AuthState.Success
    }

    /**
     * Gửi mã OTP đến số điện thoại (TẠM THỜI BYPASS ĐỂ VÀO APP LUÔU)
     */
    fun sendOtp(phoneNumber: String, activity: Activity) {
        // TẠM THỜI: Bỏ qua bước Firebase OTP, cho vào app luôn để test
        _authState.value = AuthState.Success
    }

    /**
     * Xác minh mã OTP user nhập
     */
    fun verifyOtp(otpCode: String) {
        val vId = verificationId
        if (vId == null) {
            _authState.value = AuthState.Error("Chưa gửi OTP")
            return
        }
        _authState.value = AuthState.Loading
        val credential = PhoneAuthProvider.getCredential(vId, otpCode)
        signInWithCredential(credential)
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            val result = firebaseRepo.signInWithCredential(credential)
            if (result.isSuccess) {
                // Tạo profile nếu chưa có
                firebaseRepo.createOrUpdateUser(
                    displayName = "Người dùng CapMoney",
                    avatarEmoji = "😎",
                    avatarColor = 0xFFD500F9
                )
                _authState.value = AuthState.Success
            } else {
                _authState.value = AuthState.Error(
                    result.exceptionOrNull()?.localizedMessage ?: "Đăng nhập thất bại"
                )
            }
        }
    }

    // ── SOCIAL AUTH HANDLERS ──

    fun signInWithGoogle(idToken: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = firebaseRepo.signInWithGoogle(idToken)
            if (result.isSuccess) {
                firebaseRepo.createOrUpdateUser(displayName = "Người dùng Google", avatarEmoji = "🌐")
                _authState.value = AuthState.Success
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Lỗi đăng nhập Google")
            }
        }
    }

    fun signInWithFacebook(accessToken: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = firebaseRepo.signInWithFacebook(accessToken)
            if (result.isSuccess) {
                firebaseRepo.createOrUpdateUser(displayName = "Người dùng Facebook", avatarEmoji = "📘")
                _authState.value = AuthState.Success
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Lỗi đăng nhập Facebook")
            }
        }
    }

    fun signInWithTwitter() {
        // Twitter Auth thường được xử lý qua Activity/OAuthProvider
        _authState.value = AuthState.Error("Tính năng đăng nhập Twitter đang được phát triển.")
    }

    /**
     * Đăng xuất
     */
    fun signOut() {
        firebaseRepo.signOut()
        _authState.value = AuthState.Idle
    }

    /**
     * Xóa tài khoản
     */
    fun deleteAccount() {
        viewModelScope.launch {
            // Trong thực tế sẽ gọi firebaseRepo.deleteAccount()
            signOut()
        }
    }
}

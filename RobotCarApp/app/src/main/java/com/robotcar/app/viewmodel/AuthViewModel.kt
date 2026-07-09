package com.robotcar.app.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Khai báo các trạng thái (State) có thể xảy ra của màn hình Auth
sealed class AuthState {
    object Idle : AuthState() // Trạng thái chờ, chưa làm gì
    object Loading : AuthState() // Đang xoay vòng chờ API trả lời
    data class Success(val uid: String) : AuthState() // Đăng nhập đúng, nhận UID từ Firebase
    data class Error(val message: String) : AuthState() // Lỗi sai pass, mất mạng...
}

class AuthViewModel : ViewModel() {
    
    // Thể hiện Firebase Auth
    private val auth = FirebaseAuth.getInstance()

    // Biến lưu trữ trạng thái hiện tại (Chỉ ViewModel được phép sửa đổi)
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    // Biến để giao diện quan sát (Chỉ đọc)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // Hàm thực hiện Đăng Nhập bằng Firebase
    fun login(email: String, pass: String) {
        _authState.value = AuthState.Loading // Báo cho UI hiện vòng xoay
        
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    _authState.value = AuthState.Success(user?.uid ?: "") // Báo thành công
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Lỗi đăng nhập. Vui lòng kiểm tra lại tài khoản hoặc mật khẩu.")
                }
            }
    }

    // Hàm thực hiện Đăng Ký bằng Firebase
    fun register(email: String, pass: String) {
        _authState.value = AuthState.Loading
        
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Đăng ký thành công thì Firebase tự động đăng nhập luôn
                    val user = auth.currentUser
                    _authState.value = AuthState.Success(user?.uid ?: "")
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Lỗi đăng ký. Tài khoản đã tồn tại hoặc mật khẩu quá yếu.")
                }
            }
    }

    // Hàm thực hiện Đăng Nhập bằng Google
    fun loginWithGoogle(idToken: String) {
        _authState.value = AuthState.Loading
        val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    _authState.value = AuthState.Success(user?.uid ?: "")
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Lỗi đăng nhập Google.")
                }
            }
    }

    // Hàm gửi email Đặt lại mật khẩu
    fun resetPassword(email: String) {
        if (email.isBlank()) {
            _authState.value = AuthState.Error("Vui lòng nhập Email trước khi khôi phục.")
            return
        }
        _authState.value = AuthState.Loading
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Error("Thành công! Vui lòng kiểm tra hộp thư Email của bạn để đổi mật khẩu mới.")
                    // Ở đây mượn tạm State.Error để hiển thị thông báo text cho tiện (vì Error đang render màu đỏ/vàng trên màn hình)
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Lỗi khôi phục mật khẩu. Email chưa đăng ký?")
                }
            }
    }
    
    // Đặt lại trạng thái khi người dùng đã đọc xong thông báo lỗi
    fun resetState() {
        _authState.value = AuthState.Idle
    }
}

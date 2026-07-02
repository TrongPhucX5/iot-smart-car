package com.robotcar.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robotcar.app.network.AuthRequest
import com.robotcar.app.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// 1. Khai báo các trạng thái (State) có thể xảy ra của màn hình Auth
sealed class AuthState {
    object Idle : AuthState() // Trạng thái chờ, chưa làm gì
    object Loading : AuthState() // Đang xoay vòng chờ API trả lời
    data class Success(val token: String) : AuthState() // Đăng nhập đúng, nhận JWT Token
    data class Error(val message: String) : AuthState() // Lỗi sai pass, mất mạng...
}

class AuthViewModel : ViewModel() {
    
    // Biến lưu trữ trạng thái hiện tại (Chỉ ViewModel được phép sửa đổi)
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    // Biến để giao diện quan sát (Chỉ đọc)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // Hàm thực hiện Đăng Nhập
    fun login(username: String, pass: String) {
        // Mở một luồng chạy ngầm (Coroutine) để không làm đơ giao diện
        viewModelScope.launch {
            _authState.value = AuthState.Loading // Báo cho UI hiện vòng xoay
            
            try {
                // Gọi API tới Spring Boot
                val response = RetrofitClient.instance.login(AuthRequest(username, pass))
                
                if (response.isSuccessful && response.body() != null) {
                    val token = response.body()!!.token
                    _authState.value = AuthState.Success(token) // Báo thành công
                } else {
                    _authState.value = AuthState.Error("Sai tài khoản hoặc mật khẩu (Mã lỗi ${response.code()})")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Lỗi kết nối mạng: Kiểm tra lại WiFi hoặc IP Server")
            }
        }
    }

    // Hàm thực hiện Đăng Ký
    fun register(username: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = RetrofitClient.instance.register(AuthRequest(username, pass))
                if (response.isSuccessful) {
                    // Mẹo: Đăng ký thành công trên Spring Boot thì tự động gọi hàm Login luôn để lấy Token
                    login(username, pass)
                } else {
                    _authState.value = AuthState.Error("Tài khoản đã tồn tại (Mã lỗi ${response.code()})")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Lỗi kết nối mạng: Kiểm tra lại WiFi hoặc IP Server")
            }
        }
    }
    
    // Đặt lại trạng thái khi người dùng đã đọc xong thông báo lỗi
    fun resetState() {
        _authState.value = AuthState.Idle
    }
}

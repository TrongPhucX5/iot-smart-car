package com.robotcar.app.network

// Gói tin gửi đi khi Đăng nhập hoặc Đăng ký
data class AuthRequest(
    val username: String,
    val password: String
)

// Gói tin nhận về khi Đăng nhập thành công
data class AuthResponse(
    val token: String,
    val type: String
)

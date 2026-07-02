package com.robotcar.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// Thêm 2 thư viện này để dùng ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.robotcar.app.viewmodel.AuthState
import com.robotcar.app.viewmodel.AuthViewModel

@Composable
fun AuthScreen(
    viewModel: AuthViewModel = viewModel(), // Tiêm ViewModel vào
    onAuthSuccess: (String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoginMode by remember { mutableStateOf(true) }
    
    // Theo dõi trạng thái từ ViewModel
    val authState by viewModel.authState.collectAsState()

    // Logic kiểm tra khi trạng thái chuyển sang Success
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            val token = (authState as AuthState.Success).token
            onAuthSuccess(token) // Bắn Token ra ngoài (cho MainActivity)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("ROBOT CAR CONTROL", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 32.dp))
        Text(if (isLoginMode) "Đăng Nhập Hệ Thống" else "Đăng Ký Tài Khoản", fontSize = 18.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 16.dp))

        OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Tài khoản") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Mật khẩu") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(24.dp))

        // Hiển thị vòng xoay nếu trạng thái đang là Loading, ngược lại hiện nút bấm
        if (authState is AuthState.Loading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    if (username.isNotBlank() && password.isNotBlank()) {
                        if (isLoginMode) viewModel.login(username, password)
                        else viewModel.register(username, password)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text(if (isLoginMode) "ĐĂNG NHẬP" else "ĐĂNG KÝ")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        TextButton(
            onClick = { 
                isLoginMode = !isLoginMode 
                viewModel.resetState() // Chuyển chế độ thì xóa thông báo lỗi cũ
            }
        ) {
            Text(if (isLoginMode) "Chưa có tài khoản? Đăng ký ngay" else "Đã có tài khoản? Đăng nhập", color = MaterialTheme.colorScheme.secondary)
        }

        // Hiển thị dòng chữ báo lỗi màu đỏ nếu trạng thái là Error
        if (authState is AuthState.Error) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = (authState as AuthState.Error).message,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

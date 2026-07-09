package com.robotcar.app.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.robotcar.app.R
import com.robotcar.app.viewmodel.AuthState
import com.robotcar.app.viewmodel.AuthViewModel

@Composable
fun AuthScreen(
    viewModel: AuthViewModel = viewModel(),
    onAuthSuccess: (String) -> Unit
) {
    var showWelcome by remember { mutableStateOf(true) }
    var isLoginMode by remember { mutableStateOf(true) }

    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            val token = (authState as AuthState.Success).uid
            onAuthSuccess(token)
        }
    }

    if (showWelcome) {
        WelcomeScreen(
            onSignInClick = {
                isLoginMode = true
                showWelcome = false
            },
            onSignUpClick = {
                isLoginMode = false
                showWelcome = false
            }
        )
    } else {
        AuthFormScreen(
            isLoginMode = isLoginMode,
            authState = authState,
            onBackClick = { 
                showWelcome = true 
                viewModel.resetState()
            },
            onToggleMode = {
                isLoginMode = !isLoginMode
                viewModel.resetState()
            },
            onAuthenticate = { user, pass ->
                if (isLoginMode) viewModel.login(user, pass)
                else viewModel.register(user, pass)
            },
            onGoogleSignIn = { idToken ->
                viewModel.loginWithGoogle(idToken)
            },
            onResetPassword = { email ->
                viewModel.resetPassword(email)
            }
        )
    }
}

@Composable
fun WelcomeScreen(onSignInClick: () -> Unit, onSignUpClick: () -> Unit) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val buttonColor = MaterialTheme.colorScheme.primary
    val textColor = Color.Black

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))
        
        Image(
            painter = painterResource(id = R.drawable.car_image),
            contentDescription = "Smart Car Image",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Text(
            text = "Bắt đầu ngay",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "Điều khiển xe từ xa qua ứng dụng di động.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = onSignInClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
        ) {
            Text("ĐĂNG NHẬP", color = textColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = onSignUpClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
        ) {
            Text("ĐĂNG KÝ", color = textColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthFormScreen(
    isLoginMode: Boolean,
    authState: AuthState,
    onBackClick: () -> Unit,
    onToggleMode: () -> Unit,
    onAuthenticate: (String, String) -> Unit,
    onGoogleSignIn: (String) -> Unit,
    onResetPassword: (String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showResetDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }

    val backgroundColor = MaterialTheme.colorScheme.background
    val buttonColor = MaterialTheme.colorScheme.primary
    val textColor = Color.Black

    val context = LocalContext.current
    
    // Cấu hình Google Sign-In
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val googleAuthLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account?.idToken?.let { idToken ->
                    onGoogleSignIn(idToken)
                }
            } catch (e: ApiException) {
                // Xử lý lỗi (nếu cần)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Nút quay lại ở góc trái trên
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Ảnh xe thu nhỏ
        Image(
            painter = painterResource(id = R.drawable.car_image),
            contentDescription = "Smart Car Image",
            modifier = Modifier
                .height(120.dp)
                .fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = if (isLoginMode) "Đăng Nhập" else "Đăng Ký",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Tài khoản (Email)", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = buttonColor,
                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Mật khẩu", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = buttonColor,
                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground
            )
        )
        
        if (isLoginMode) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(end = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { showResetDialog = true }) {
                    Text("Quên mật khẩu?", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
                }
            }
        } else {
            Spacer(modifier = Modifier.height(32.dp))
        }

        if (authState is AuthState.Loading) {
            CircularProgressIndicator(color = buttonColor)
        } else {
            Button(
                onClick = {
                    if (username.isNotBlank() && password.isNotBlank()) {
                        onAuthenticate(username, password)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
            ) {
                Text(if (isLoginMode) "ĐĂNG NHẬP" else "ĐĂNG KÝ", color = textColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Nút Đăng nhập Google
            OutlinedButton(
                onClick = {
                    googleAuthLauncher.launch(googleSignInClient.signInIntent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onBackground),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.google),
                    contentDescription = "Google Icon",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("Đăng nhập bằng Google", fontWeight = FontWeight.Medium, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onToggleMode) {
            Text(
                text = if (isLoginMode) "Chưa có tài khoản? Đăng ký ngay" else "Đã có tài khoản? Đăng nhập",
                color = buttonColor
            )
        }

        if (authState is AuthState.Error) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = (authState as AuthState.Error).message,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }

    // Dialog Quên Mật Khẩu
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Khôi phục mật khẩu") },
            text = {
                Column {
                    Text("Nhập email của bạn để nhận liên kết đặt lại mật khẩu.")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text("Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onResetPassword(resetEmail)
                        showResetDialog = false
                    }
                ) {
                    Text("Gửi")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}

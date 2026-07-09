package com.robotcar.app

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.robotcar.app.ui.screens.AuthScreen
import com.robotcar.app.ui.screens.ControlDashboardScreen
import com.robotcar.app.ui.screens.MainScreen
import com.robotcar.app.ui.theme.RobotCarAppTheme

// Hàm phụ trợ để tìm Activity từ Context
fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

// Khóa hướng màn hình cho từng Composable
@Composable
fun LockScreenOrientation(orientation: Int) {
    val context = LocalContext.current
    DisposableEffect(orientation) {
        val activity = context.findActivity() ?: return@DisposableEffect onDispose {}
        val originalOrientation = activity.requestedOrientation
        activity.requestedOrientation = orientation
        onDispose {
            // Có thể phục hồi orientation gốc nếu cần, nhưng ta thiết lập cụ thể cho mỗi màn hình rồi
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val sharedPref = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        
        setContent {
            // Đặt mặc định là false (Light theme) theo ý người dùng
            var isDarkTheme by remember { 
                mutableStateOf(sharedPref.getBoolean("isDarkTheme", false)) 
            }
            
            RobotCarAppTheme(darkTheme = isDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    
                    val navController = rememberNavController()
                    
                    val auth = FirebaseAuth.getInstance()
                    val currentUser = auth.currentUser

                    // Nếu đã đăng nhập thì vào thẳng MainScreen (chứa Trang chủ)
                    val startDestination = if (currentUser != null) "main" else "auth"

                    NavHost(navController = navController, startDestination = startDestination) {
                        
                        composable("auth") {
                            LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                            AuthScreen(
                                onAuthSuccess = { _ ->
                                    navController.navigate("main") {
                                        popUpTo("auth") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // Màn hình chính (chứa Bottom Nav: Home, Lịch sử, Cài đặt)
                        composable("main") {
                            MainScreen(
                                token = auth.currentUser?.uid ?: "",
                                vehicleId = 1L,
                                isDarkTheme = isDarkTheme,
                                onThemeChange = { newTheme ->
                                    isDarkTheme = newTheme
                                    sharedPref.edit().putBoolean("isDarkTheme", newTheme).apply()
                                },
                                onNavigateToControl = {
                                    navController.navigate("control")
                                },
                                onLogout = {
                                    auth.signOut()
                                    navController.navigate("auth") {
                                        popUpTo("main") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // Màn hình Điều khiển xe (Nằm ngang toàn màn hình)
                        composable("control") {
                            LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE)
                            ControlDashboardScreen(
                                token = auth.currentUser?.uid ?: "", 
                                vehicleId = 1L, // Mặc định là xe số 1
                                onBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        
                    }
                }
            }
        }
    }
}
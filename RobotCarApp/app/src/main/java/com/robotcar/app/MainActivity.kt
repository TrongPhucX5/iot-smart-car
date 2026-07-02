package com.robotcar.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.robotcar.app.ui.screens.AuthScreen
import com.robotcar.app.ui.screens.VehicleListScreen
import com.robotcar.app.ui.screens.ControlDashboardScreen
import com.robotcar.app.ui.theme.RobotCarAppTheme
import com.robotcar.app.utils.TokenManager
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Khởi tạo két sắt lưu Token
        val tokenManager = TokenManager(this)

        setContent {
            RobotCarAppTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    
                    // 1. Khởi tạo bộ điều khiển màn hình
                    val navController = rememberNavController()
                    val coroutineScope = rememberCoroutineScope()
                    
                    // 2. Lấy Token từ bộ nhớ ra kiểm tra xem đã login chưa
                    val savedToken by tokenManager.tokenFlow.collectAsState(initial = null)

                    // 3. Nếu có Token rồi thì nhảy thẳng vào Danh sách xe, chưa có thì vào Đăng nhập
                    val startDestination = if (savedToken != null) "vehicle_list" else "auth"

                    NavHost(navController = navController, startDestination = startDestination) {
                        
                        // Khai báo lộ trình màn hình Đăng Nhập
                        composable("auth") {
                            AuthScreen(
                                onAuthSuccess = { token ->
                                    coroutineScope.launch {
                                        tokenManager.saveToken(token) // Lưu token xuống DataStore
                                        // Chuyển sang màn hình xe và xóa lịch sử lùi lại (backstack)
                                        navController.navigate("vehicle_list") {
                                            popUpTo("auth") { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }

                        // Khai báo lộ trình màn hình Danh sách xe
                        composable("vehicle_list") {
                            VehicleListScreen(
                                token = savedToken!!,
                                onVehicleSelected = { vehicleId ->
                                    // Khi bấm vào 1 thẻ xe, điều hướng sang trang control kèm ID
                                    navController.navigate("control/$vehicleId")
                                },
                                onLogout = {
                                    coroutineScope.launch {
                                        tokenManager.clearToken() // Xóa token
                                        navController.navigate("auth") {
                                            popUpTo("vehicle_list") { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }

                        // THÊM MỚI ĐOẠN NÀY ĐỂ NHẬN ĐIỀU HƯỚNG
                        composable("control/{vehicleId}") { backStackEntry ->
                            // Rút xuất vehicleId từ URL ra
                            val vehicleIdStr = backStackEntry.arguments?.getString("vehicleId")
                            val vehicleId = vehicleIdStr?.toLongOrNull() ?: 0L

                            ControlDashboardScreen(
                                token = savedToken!!,
                                vehicleId = vehicleId,
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                        
                    }
                }
            }
        }
    }
}
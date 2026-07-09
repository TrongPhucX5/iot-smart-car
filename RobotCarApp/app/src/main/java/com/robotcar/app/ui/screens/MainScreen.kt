package com.robotcar.app.ui.screens

import android.content.pm.ActivityInfo
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.robotcar.app.LockScreenOrientation

@Composable
fun MainScreen(
    token: String,
    vehicleId: Long,
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    onNavigateToControl: () -> Unit,
    onLogout: () -> Unit
) {
    // Luôn khóa màn hình dọc cho toàn bộ khu vực Main
    LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
    
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Trang Chủ") },
                    label = { Text("Trang Chủ") },
                    selected = currentRoute == "home",
                    onClick = {
                        bottomNavController.navigate("home") {
                            popUpTo(bottomNavController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.List, contentDescription = "Lịch Sử") },
                    label = { Text("Lịch Sử") },
                    selected = currentRoute == "stats",
                    onClick = {
                        bottomNavController.navigate("stats") {
                            popUpTo(bottomNavController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Settings, contentDescription = "Cài Đặt") },
                    label = { Text("Cài Đặt") },
                    selected = currentRoute == "settings",
                    onClick = {
                        bottomNavController.navigate("settings") {
                            popUpTo(bottomNavController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = bottomNavController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home") {
                HomeScreen(
                    token = token,
                    vehicleId = vehicleId,
                    onNavigateToControl = onNavigateToControl
                )
            }
            composable("stats") {
                StatsScreen(vehicleId = vehicleId)
            }
            composable("settings") {
                SettingsScreen(
                    isDarkTheme = isDarkTheme,
                    onThemeChange = onThemeChange,
                    onLogout = onLogout
                )
            }
        }
    }
}

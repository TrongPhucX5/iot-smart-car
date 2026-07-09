package com.robotcar.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.robotcar.app.viewmodel.SensorViewModel

@Composable
fun SensorMonitorScreen(
    token: String,
    vehicleId: Long,
    viewModel: SensorViewModel = viewModel()
) {
    // Quan sát state từ Firebase thông qua ViewModel
    val sensorData by viewModel.sensorData.collectAsState()

    // Kích hoạt Listener Firebase ngay khi vào màn hình
    LaunchedEffect(Unit) {
        viewModel.startPolling(token, vehicleId)
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text("DỮ LIỆU TỪ XE", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            if (sensorData == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Đang kết nối với xe...", modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                // 1. Trạng thái kết nối
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Trạng thái: ", fontWeight = FontWeight.Bold)
                    Text(
                        text = if (sensorData!!.is_online) "Online (Đang hoạt động)" else "Offline (Mất tín hiệu)",
                        color = if (sensorData!!.is_online) Color(0xFF4CAF50) else Color.Red,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Cảm biến Siêu âm (Khoảng cách)
                val distance = sensorData!!.obstacle_distance
                val distanceProgress = (distance / 100f).coerceIn(0f, 1f) // Giả sử max là 100cm
                SensorBar(
                    label = "Khoảng cách vật cản: ${String.format("%.1f", distance)} cm",
                    progress = distanceProgress,
                    color = if (distance < 15f) Color.Red else Color(0xFF4CAF50) // Đỏ nếu gần đâm
                )
            }
        }
    }
}

// Component vẽ thanh tiến trình
@Composable
fun SensorBar(label: String, progress: Float, color: Color) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxWidth().height(10.dp),
            color = color,
            trackColor = Color.LightGray
        )
    }
}

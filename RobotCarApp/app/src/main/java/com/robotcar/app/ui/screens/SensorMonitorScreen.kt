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
    // Quan sát state từ ViewModel
    val sensorData by viewModel.sensorData.collectAsState()

    // Kích hoạt Polling ngay khi vào màn hình
    LaunchedEffect(Unit) {
        viewModel.startPolling(token, vehicleId)
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text("DỮ LIỆU CẢM BIẾN", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            if (sensorData == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                Text("Đang chờ dữ liệu từ xe...", modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                // 1. Cảm biến Siêu âm (Khoảng cách)
                val distance = sensorData!!.distance
                val distanceProgress = (distance / 100f).coerceIn(0f, 1f) // Giả sử max là 100cm
                SensorBar(
                    label = "Khoảng cách vật cản: ${distance}cm",
                    progress = distanceProgress,
                    color = if (distance < 15f) Color.Red else Color(0xFF4CAF50) // Đỏ nếu gần đâm
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 2. Cảm biến Ánh sáng
                val light = sensorData!!.lightLevel
                val lightProgress = (light / 1024f).coerceIn(0f, 1f) // ESP32 ADC thường max là 1024 hoặc 4095
                SensorBar(
                    label = "Cường độ ánh sáng: $light",
                    progress = lightProgress,
                    color = Color(0xFFFFC107) // Màu vàng
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 3. Cảm biến Dò Line (Trạng thái)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Trạng thái Dò Line: ", fontWeight = FontWeight.Bold)
                    Text(
                        text = sensorData!!.lineStatus,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
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

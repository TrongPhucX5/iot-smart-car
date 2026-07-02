package com.robotcar.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.robotcar.app.viewmodel.VehicleViewModel

@Composable
fun VehicleListScreen(
    token: String, // Nhận token từ MainActivity truyền vào
    viewModel: VehicleViewModel = viewModel(),
    onVehicleSelected: (Long) -> Unit,
    onLogout: () -> Unit
) {
    // Quan sát State từ ViewModel
    val vehicles by viewModel.vehicles.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Gọi API ngay khi màn hình vừa mở lên
    LaunchedEffect(Unit) {
        viewModel.fetchVehicles(token)
    }

    Scaffold(
        topBar = {
            // Thanh tiêu đề phía trên
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Danh Sách Xe Robot", fontWeight = FontWeight.Bold) },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Đăng Xuất", color = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (vehicles.isEmpty()) {
                Text("Chưa có xe nào trong hệ thống", modifier = Modifier.align(Alignment.Center))
            } else {
                // Vẽ danh sách Card
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(vehicles) { vehicle ->
                        VehicleCard(
                            vehicleName = vehicle.vehicleName,
                            status = vehicle.status,
                            mode = vehicle.currentMode,
                            onClick = { onVehicleSelected(vehicle.vehicleId) }
                        )
                    }
                }
            }
        }
    }
}

// Component vẽ riêng cho từng thẻ xe
@Composable
fun VehicleCard(vehicleName: String, status: String, mode: String, onClick: () -> Unit) {
    val isOnline = status == "ONLINE"
    
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = vehicleName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Chế độ: $mode", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }
            
            // Hiển thị trạng thái Online/Offline
            Surface(
                color = if (isOnline) Color(0xFF4CAF50) else Color.Red, // Xanh lá hoặc Đỏ
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = status,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

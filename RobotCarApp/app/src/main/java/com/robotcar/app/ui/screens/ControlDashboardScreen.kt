package com.robotcar.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.robotcar.app.viewmodel.VehicleControlViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControlDashboardScreen(
    token: String,
    vehicleId: Long,
    onBackClick: () -> Unit = {},
    viewModel: VehicleControlViewModel = viewModel()
) {
    // Tạm lưu mode hiện tại trên UI để đổi màu
    var currentMode by remember { mutableStateOf("MANUAL") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("BẢNG ĐIỀU KHIỂN (XE #$vehicleId)", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // CỘT TRÁI: Cụm phím D-PAD
            Box(
                modifier = Modifier.size(250.dp),
                contentAlignment = Alignment.Center
            ) {
                DirectionButton(modifier = Modifier.align(Alignment.TopCenter), text = "TIẾN") {
                    viewModel.sendCommand(token, vehicleId, "FORWARD")
                }
                DirectionButton(modifier = Modifier.align(Alignment.BottomCenter), text = "LÙI") {
                    viewModel.sendCommand(token, vehicleId, "BACKWARD")
                }
                DirectionButton(modifier = Modifier.align(Alignment.CenterStart), text = "TRÁI") {
                    viewModel.sendCommand(token, vehicleId, "LEFT")
                }
                DirectionButton(modifier = Modifier.align(Alignment.CenterEnd), text = "PHẢI") {
                    viewModel.sendCommand(token, vehicleId, "RIGHT")
                }
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.Red)
                        .clickable { viewModel.sendCommand(token, vehicleId, "STOP") },
                    contentAlignment = Alignment.Center
                ) {
                    Text("STOP", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            // CỘT PHẢI: Chọn chế độ & Cảm biến
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                    ModeButton("MANUAL", currentMode) { 
                        currentMode = "MANUAL"
                        viewModel.changeMode(token, vehicleId, "MANUAL")
                    }
                    ModeButton("LINE_TRACKING", currentMode) { 
                        currentMode = "LINE_TRACKING"
                        viewModel.changeMode(token, vehicleId, "LINE_TRACKING")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Hiện thông số cảm biến realtime
                SensorMonitorScreen(token = token, vehicleId = vehicleId)
            }
        }
    }
}

// Component phụ để vẽ phím D-Pad
@Composable
fun DirectionButton(modifier: Modifier = Modifier, text: String, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .size(70.dp)
            .clip(CircleShape)
            .background(Color.DarkGray)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

// Component phụ để vẽ nút Mode
@Composable
fun ModeButton(mode: String, currentMode: String, onClick: () -> Unit) {
    val isSelected = mode == currentMode
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
            contentColor = if (isSelected) Color.White else Color.Black
        )
    ) {
        Text(mode)
    }
}

package com.robotcar.app.ui.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.robotcar.app.utils.LocationHelper
import com.robotcar.app.viewmodel.VehicleControlViewModel
import kotlin.math.roundToInt

import com.robotcar.app.viewmodel.SensorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControlDashboardScreen(
    token: String,
    vehicleId: Long,
    onBack: () -> Unit = {},
    viewModel: VehicleControlViewModel = viewModel(),
    sensorViewModel: SensorViewModel = viewModel()
) {
    // Đọc trạng thái kết nối THỰC TẾ từ Firebase
    val sensorData by sensorViewModel.sensorData.collectAsState()
    val isConnected = sensorData?.is_online == true

    // Trạng thái tốc độ (0 - 255)
    var speed by remember { mutableFloatStateOf(150f) }

    LaunchedEffect(Unit) {
        sensorViewModel.startPolling(token, vehicleId)
    }
    
    // Cảm biến nghiêng
    var gravityEnabled by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    var pitch by remember { mutableStateOf(0f) } // Lên/Xuống
    var roll by remember { mutableStateOf(0f) }  // Trái/Phải

    // Xin quyền và lấy vị trí GPS
    val locationHelper = remember { LocationHelper(context) }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                      permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            locationHelper.fetchAndSaveLocation()
        }
    }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    DisposableEffect(gravityEnabled) {
        // Đổi từ TYPE_ACCELEROMETER sang TYPE_GRAVITY để loại bỏ nhiễu khi rung lắc
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (!gravityEnabled) return
                
                // Màn hình ngang (Landscape): X là trái/phải, Y là tới/lui
                val x = event.values[1] // Trục Y của điện thoại khi đứng sẽ thành trục X khi cầm ngang
                val y = event.values[0] // Trục X của điện thoại khi đứng sẽ thành trục Y khi cầm ngang
                
                roll = y // Độ nghiêng trái phải
                pitch = x // Độ nghiêng tới lui
                
                // Gửi lệnh tương ứng với độ nghiêng
                if (isConnected) {
                    if (pitch < -3f) viewModel.sendCommand(token, vehicleId, "FORWARD")
                    else if (pitch > 3f) viewModel.sendCommand(token, vehicleId, "BACKWARD")
                    else if (roll > 3f) viewModel.sendCommand(token, vehicleId, "RIGHT")
                    else if (roll < -3f) viewModel.sendCommand(token, vehicleId, "LEFT")
                    else viewModel.sendCommand(token, vehicleId, "STOP")
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        
        if (gravityEnabled) {
            sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)
        }
        
        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ĐIỀU KHIỂN XE", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isConnected) Color(0xFF4CAF50) else Color.Red
                        ),
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Text(if (isConnected) "Đã kết nối" else "Mất tín hiệu", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                Joystick(
                    onMove = { x, y ->
                        if (isConnected && !gravityEnabled) {
                            if (y < -0.5f) viewModel.sendCommand(token, vehicleId, "FORWARD")
                            else if (y > 0.5f) viewModel.sendCommand(token, vehicleId, "BACKWARD")
                            else if (x < -0.5f) viewModel.sendCommand(token, vehicleId, "LEFT")
                            else if (x > 0.5f) viewModel.sendCommand(token, vehicleId, "RIGHT")
                            else viewModel.sendCommand(token, vehicleId, "STOP")
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = { },
                        modifier = Modifier.pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    if (isConnected && !gravityEnabled) viewModel.sendCommand(token, vehicleId, "SPIN_LEFT")
                                    tryAwaitRelease()
                                    if (isConnected && !gravityEnabled) viewModel.sendCommand(token, vehicleId, "STOP")
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Text("Xoay Trái", color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { },
                        modifier = Modifier.pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    if (isConnected && !gravityEnabled) viewModel.sendCommand(token, vehicleId, "SPIN_RIGHT")
                                    tryAwaitRelease()
                                    if (isConnected && !gravityEnabled) viewModel.sendCommand(token, vehicleId, "STOP")
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Text("Xoay Phải", color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Tốc độ: ${speed.toInt()}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                        Slider(
                            value = speed,
                            onValueChange = { speed = it },
                            onValueChangeFinished = { 
                                if (isConnected) viewModel.setSpeed(token, vehicleId, speed.toInt()) 
                            },
                            valueRange = 0f..255f,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Cảm biến nghiêng", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = gravityEnabled,
                                onCheckedChange = { gravityEnabled = it }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.size(width = 100.dp, height = 2.dp).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)))
                    Box(modifier = Modifier.size(width = 2.dp, height = 100.dp).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)))
                    
                    val maxTravel = 40f
                    val dotX = if (gravityEnabled) (roll * 10f).coerceIn(-maxTravel, maxTravel) else 0f
                    val dotY = if (gravityEnabled) (pitch * 10f).coerceIn(-maxTravel, maxTravel) else 0f
                    
                    Box(
                        modifier = Modifier
                            .offset { IntOffset(dotX.roundToInt(), dotY.roundToInt()) }
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(if (gravityEnabled) MaterialTheme.colorScheme.primary else Color.Gray)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                Row {
                    Text(
                        text = "PING: ${sensorData?.last_ping ?: 0}ms",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Vật cản: ${sensorData?.obstacle_distance ?: 0} cm",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun Joystick(onMove: (Float, Float) -> Unit) {
    val maxRadius = 150f
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .size(160.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        offsetX = 0f
                        offsetY = 0f
                        onMove(0f, 0f)
                    },
                    onDragCancel = {
                        offsetX = 0f
                        offsetY = 0f
                        onMove(0f, 0f)
                    }
                ) { change, dragAmount ->
                    change.consume()
                    val newX = offsetX + dragAmount.x
                    val newY = offsetY + dragAmount.y
                    
                    val distance = kotlin.math.sqrt((newX * newX + newY * newY).toDouble()).toFloat()
                    
                    if (distance <= maxRadius) {
                        offsetX = newX
                        offsetY = newY
                    } else {
                        // Giới hạn chấm tròn không lọt ra ngoài hình tròn
                        offsetX = (newX / distance) * maxRadius
                        offsetY = (newY / distance) * maxRadius
                    }
                    
                    // Trả về giá trị từ -1.0 đến 1.0 cho View Model xử lý
                    onMove(offsetX / maxRadius, offsetY / maxRadius)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .size(60.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFD15C)) // Màu vàng đặc trưng của app
        )
    }
}

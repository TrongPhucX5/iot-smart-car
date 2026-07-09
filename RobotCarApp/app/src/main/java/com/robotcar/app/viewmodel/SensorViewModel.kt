package com.robotcar.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.robotcar.app.model.SensorData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SensorViewModel : ViewModel() {

    private val _sensorData = MutableStateFlow<SensorData?>(SensorData())
    val sensorData: StateFlow<SensorData?> = _sensorData.asStateFlow()
    
    private val database = FirebaseDatabase.getInstance().reference

    private var lastPingValue: Long = 0L
    private var lastPingReceivedTime: Long = 0L

    init {
        // Watchdog timer để kiểm tra kết nối
        viewModelScope.launch {
            while (true) {
                delay(1000)
                // Nếu sau 4 giây không nhận được ping mới từ ESP32 -> Xe đã tắt nguồn
                if (System.currentTimeMillis() - lastPingReceivedTime > 4000) {
                    val currentData = _sensorData.value
                    if (currentData != null && currentData.is_online) {
                        _sensorData.value = currentData.copy(is_online = false)
                    }
                }
            }
        }
    }

    // Bắt đầu lắng nghe dữ liệu cảm biến Realtime từ Firebase
    fun startPolling(token: String, vehicleId: Long) {
        val sensorRef = database.child("vehicles").child(vehicleId.toString()).child("sensors")
        
        sensorRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val distance = snapshot.child("obstacle_distance").getValue(Float::class.java) ?: 0f
                    val ping = snapshot.child("last_ping").getValue(Long::class.java) ?: 0L
                    
                    var isOnline = _sensorData.value?.is_online ?: false
                    
                    // Nếu giá trị ping từ ESP32 thay đổi, nghĩa là ESP32 đang còn sống
                    if (ping != lastPingValue) {
                        lastPingValue = ping
                        lastPingReceivedTime = System.currentTimeMillis()
                        isOnline = true
                    }
                    
                    _sensorData.value = SensorData(
                        obstacle_distance = distance,
                        is_online = isOnline,
                        last_ping = ping
                    )
                } catch (e: Exception) {
                    Log.e("SENSOR", "Lỗi parse dữ liệu cảm biến: ${e.message}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("SENSOR", "Lỗi Firebase: ${error.message}")
            }
        })
    }
}

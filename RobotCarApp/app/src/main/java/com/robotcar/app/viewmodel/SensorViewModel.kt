package com.robotcar.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robotcar.app.network.RetrofitClient
import com.robotcar.app.network.SensorResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SensorViewModel : ViewModel() {

    private val _sensorData = MutableStateFlow<SensorResponse?>(null)
    val sensorData: StateFlow<SensorResponse?> = _sensorData.asStateFlow()

    // Hàm bắt đầu vòng lặp lấy dữ liệu
    fun startPolling(token: String, vehicleId: Long) {
        viewModelScope.launch {
            // Vòng lặp sẽ chạy liên tục chừng nào màn hình này còn mở
            while (isActive) {
                try {
                    val response = RetrofitClient.instance.getLatestSensor("Bearer $token", vehicleId)
                    if (response.isSuccessful && response.body() != null) {
                        _sensorData.value = response.body()
                    }
                } catch (e: Exception) {
                    Log.e("SENSOR", "Lỗi lấy dữ liệu cảm biến: ${e.message}")
                }
                
                // Nghỉ 1000ms (1 giây) rồi mới gọi lại API để tránh làm sập server Azure
                delay(1000) 
            }
        }
    }
}

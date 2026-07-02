package com.robotcar.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robotcar.app.network.CommandRequest
import com.robotcar.app.network.ModeRequest
import com.robotcar.app.network.RetrofitClient
import kotlinx.coroutines.launch

class VehicleControlViewModel : ViewModel() {

    // Hàm bắn lệnh di chuyển
    fun sendCommand(token: String, vehicleId: Long, command: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.sendCommand("Bearer $token", CommandRequest(vehicleId, command))
                if (response.isSuccessful) {
                    Log.d("CONTROL", "Gửi lệnh $command thành công!")
                }
            } catch (e: Exception) {
                Log.e("CONTROL", "Lỗi gửi lệnh: ${e.message}")
            }
        }
    }

    // Hàm đổi chế độ
    fun changeMode(token: String, vehicleId: Long, mode: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.changeMode("Bearer $token", ModeRequest(vehicleId, mode))
                if (response.isSuccessful) {
                    Log.d("CONTROL", "Đổi sang chế độ $mode thành công!")
                }
            } catch (e: Exception) {
                Log.e("CONTROL", "Lỗi đổi chế độ: ${e.message}")
            }
        }
    }
}

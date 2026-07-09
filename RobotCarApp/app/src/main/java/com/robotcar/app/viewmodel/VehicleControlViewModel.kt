package com.robotcar.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.database.FirebaseDatabase

class VehicleControlViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance().reference

    // Hàm bắn lệnh di chuyển
    fun sendCommand(token: String, vehicleId: Long, command: String) {
        try {
            // Gửi lệnh lên nhánh: vehicles/{vehicleId}/command
            database.child("vehicles").child(vehicleId.toString()).child("command").setValue(command)
                .addOnSuccessListener {
                    Log.d("CONTROL", "Gửi lệnh $command thành công!")
                }
                .addOnFailureListener { e ->
                    Log.e("CONTROL", "Lỗi gửi lệnh: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e("CONTROL", "Lỗi Exception: ${e.message}")
        }
    }

    // Hàm chỉnh tốc độ (0 - 255)
    fun setSpeed(token: String, vehicleId: Long, speed: Int) {
        try {
            database.child("vehicles").child(vehicleId.toString()).child("speed").setValue(speed)
        } catch (e: Exception) {
            Log.e("CONTROL", "Lỗi chỉnh tốc độ: ${e.message}")
        }
    }
}

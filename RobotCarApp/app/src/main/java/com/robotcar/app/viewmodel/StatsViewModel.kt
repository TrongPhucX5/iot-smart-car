package com.robotcar.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.robotcar.app.model.LogItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class StatsViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance().reference
    
    private val _logs = MutableStateFlow<List<LogItem>>(emptyList())
    val logs: StateFlow<List<LogItem>> = _logs.asStateFlow()

    fun fetchLogs(vehicleId: Long) {
        val logsRef = database.child("vehicles").child(vehicleId.toString()).child("logs")
        
        // Dùng addValueEventListener để nhận data realtime mỗi khi có log mới
        logsRef.orderByChild("timestamp").limitToLast(50).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val logList = mutableListOf<LogItem>()
                for (child in snapshot.children) {
                    try {
                        val type = child.child("type").getValue(String::class.java) ?: "INFO"
                        val message = child.child("message").getValue(String::class.java) ?: ""
                        val timestamp = child.child("timestamp").getValue(Long::class.java) ?: 0L
                        
                        logList.add(LogItem(id = child.key ?: "", type = type, message = message, timestamp = timestamp))
                    } catch (e: Exception) {
                        Log.e("STATS", "Lỗi parse log: ${e.message}")
                    }
                }
                // Đảo ngược list để log mới nhất hiện lên trên cùng
                _logs.value = logList.reversed()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("STATS", "Lỗi đọc logs: ${error.message}")
            }
        })
    }
}

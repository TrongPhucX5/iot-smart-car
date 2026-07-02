package com.robotcar.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robotcar.app.network.RetrofitClient
import com.robotcar.app.network.VehicleResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VehicleViewModel : ViewModel() {

    private val _vehicles = MutableStateFlow<List<VehicleResponse>>(emptyList())
    val vehicles: StateFlow<List<VehicleResponse>> = _vehicles.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Hàm gọi API lấy danh sách xe, bắt buộc phải truyền Token vào
    fun fetchVehicles(token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Thêm chữ "Bearer " vào trước token theo đúng chuẩn bảo mật JWT
                val response = RetrofitClient.instance.getVehicles("Bearer $token")
                if (response.isSuccessful && response.body() != null) {
                    _vehicles.value = response.body()!!
                }
            } catch (e: Exception) {
                // Xử lý lỗi mạng ở đây
            } finally {
                _isLoading.value = false
            }
        }
    }
}

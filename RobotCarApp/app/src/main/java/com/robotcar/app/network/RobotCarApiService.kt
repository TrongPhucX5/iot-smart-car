package com.robotcar.app.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

data class VehicleResponse(
    val vehicleId: Long,
    val vehicleName: String,
    val status: String,
    val currentMode: String,
    val wifiSignal: String?
)

data class CommandRequest(val vehicleId: Long, val command: String)
data class ModeRequest(val vehicleId: Long, val mode: String)
data class SimpleResponse(val message: String, val status: String?) // Dùng chung cho các API trả về message

data class SensorResponse(
    val sensorId: Long,
    val distance: Float,
    val lightLevel: Float,
    val lineStatus: String, // "LEFT", "CENTER", "RIGHT", "LOST"
    val createdAt: String
)

interface RobotCarApiService {
    
    @POST("/api/auth/register")
    suspend fun register(
        @Body request: AuthRequest
    ): Response<Unit>

    @POST("/api/auth/login")
    suspend fun login(
        @Body request: AuthRequest
    ): Response<AuthResponse>

    @GET("/api/vehicle")
    suspend fun getVehicles(
        @Header("Authorization") bearerToken: String
    ): Response<List<VehicleResponse>>

    @POST("/api/vehicle/command")
    suspend fun sendCommand(
        @Header("Authorization") token: String,
        @Body request: CommandRequest
    ): Response<SimpleResponse>

    @POST("/api/vehicle/mode")
    suspend fun changeMode(
        @Header("Authorization") token: String,
        @Body request: ModeRequest
    ): Response<SimpleResponse>

    @GET("/api/sensor/latest")
    suspend fun getLatestSensor(
        @Header("Authorization") token: String,
        @Query("vehicleId") vehicleId: Long
    ): Response<SensorResponse>
}

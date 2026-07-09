package com.robotcar.app.model

data class SensorData(
    val obstacle_distance: Float = 0f,
    val is_online: Boolean = false,
    val last_ping: Long = 0L
)

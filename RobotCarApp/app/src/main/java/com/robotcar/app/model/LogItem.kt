package com.robotcar.app.model

data class LogItem(
    val id: String = "",
    val type: String = "",
    val message: String = "",
    val timestamp: Long = 0L
)

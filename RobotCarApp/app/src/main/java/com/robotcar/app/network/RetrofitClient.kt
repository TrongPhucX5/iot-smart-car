package com.robotcar.app.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // CHÚ Ý QUAN TRỌNG: 
    // - Nếu dùng MÁY ẢO Android (Emulator), giữ nguyên IP "10.0.2.2" để nó hiểu là localhost của máy tính.
    // - Nếu cắm ĐIỆN THOẠI THẬT qua cáp USB, hãy đổi "10.0.2.2" thành IP mạng WiFi nội bộ của máy tính bạn (VD: "http://192.168.1.5:8080").
    private const val BASE_URL = "http://192.168.1.11:8080"

    private val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
    private val client = OkHttpClient.Builder().addInterceptor(logging).build()

    val instance: RobotCarApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RobotCarApiService::class.java)
    }
}

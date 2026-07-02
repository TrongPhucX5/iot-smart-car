package com.robotcar.app.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Khởi tạo DataStore (Tương tự như LocalStorage trên Web)
private val Context.dataStore by preferencesDataStore(name = "user_prefs")

class TokenManager(private val context: Context) {
    
    // Tạo một cái "chìa khóa" để cất và lấy Token
    companion object {
        val JWT_TOKEN_KEY = stringPreferencesKey("jwt_token")
    }

    // Hàm lấy Token ra (dạng Flow để theo dõi realtime)
    val tokenFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[JWT_TOKEN_KEY]
    }

    // Hàm lưu Token vào bộ nhớ máy
    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[JWT_TOKEN_KEY] = token
        }
    }

    // Hàm xóa Token (dùng khi Đăng xuất)
    suspend fun clearToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(JWT_TOKEN_KEY)
        }
    }
}

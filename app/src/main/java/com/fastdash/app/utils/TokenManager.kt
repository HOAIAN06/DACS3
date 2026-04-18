package com.fastdash.app.utils

import android.content.Context

class TokenManager(context: Context) {

    private val prefs = context.getSharedPreferences("fastdash_prefs", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        prefs.edit().putString("jwt_token", token).apply()
    }

    fun getToken(): String? {
        return prefs.getString("jwt_token", null)
    }

    fun saveRole(role: String) {
        prefs.edit().putString("user_role", role).apply()
    }

    fun getRole(): String? {
        return prefs.getString("user_role", null)
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
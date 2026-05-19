package com.fastdash.app.utils

import android.content.Context

class TokenManager(context: Context) {

	private val prefs = context.getSharedPreferences("fastdash_prefs", Context.MODE_PRIVATE)

	fun saveUserId(id: Long) {
		prefs.edit().putLong("user_id", id).apply()
	}

	fun getUserId(): Long? {
		return if (prefs.contains("user_id")) prefs.getLong("user_id", -1L) else null
	}

	fun saveToken(token: String?) {
		prefs.edit().putString("jwt_token", token.orEmpty()).apply()
	}

	fun getToken(): String? {
		return prefs.getString("jwt_token", null)
	}

	fun saveRole(role: String?) {
		prefs.edit().putString("user_role", role.orEmpty()).apply()
	}

	fun getRole(): String? {
		return prefs.getString("user_role", null)
	}

	fun saveFullName(fullName: String?) {
		prefs.edit().putString("user_full_name", fullName.orEmpty()).apply()
	}

	fun getFullName(): String? {
		return prefs.getString("user_full_name", null)
	}

	fun saveEmail(email: String?) {
		prefs.edit().putString("user_email", email.orEmpty()).apply()
	}

	fun getEmail(): String? {
		return prefs.getString("user_email", null)
	}

	fun savePhone(phone: String?) {
		prefs.edit().putString("user_phone", phone.orEmpty()).apply()
	}

	fun getPhone(): String? {
		return prefs.getString("user_phone", null)
	}

	fun clear() {
		prefs.edit().clear().apply()
	}
}

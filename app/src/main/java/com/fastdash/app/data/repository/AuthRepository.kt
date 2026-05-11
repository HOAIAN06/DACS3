package com.fastdash.app.data.repository

import android.content.Context
import com.fastdash.app.data.model.request.LoginRequest
import com.fastdash.app.data.model.request.RegisterRequest
import com.fastdash.app.data.model.response.LoginResponse
import com.fastdash.app.data.model.response.RegisterResponse
import com.fastdash.app.data.remote.retrofit.RetrofitClient
import retrofit2.Response

class AuthRepository(private val context: Context) {

	suspend fun login(email: String, password: String): Response<LoginResponse> {
		return RetrofitClient.authApi(context).login(
			LoginRequest(email.trim().lowercase(), password)
		)
	}

	suspend fun register(
		fullName: String,
		email: String,
		phone: String,
		password: String
	): Response<RegisterResponse> {
		return RetrofitClient.authApi(context).register(
			RegisterRequest(
				fullName = fullName.trim(),
				email = email.trim().lowercase(),
				phone = phone.trim(),
				password = password
			)
		)
	}
}
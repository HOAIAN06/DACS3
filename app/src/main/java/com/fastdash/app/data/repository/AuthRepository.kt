package com.fastdash.app.data.repository

import android.content.Context
import com.fastdash.app.data.model.request.ForgotPasswordRequest
import com.fastdash.app.data.model.request.GoogleLoginRequest
import com.fastdash.app.data.model.request.LoginRequest
import com.fastdash.app.data.model.request.ResetPasswordRequest
import com.fastdash.app.data.model.request.RegisterRequest
import com.fastdash.app.data.model.request.VerifyResetCodeRequest
import com.fastdash.app.data.model.response.BasicMessageResponse
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

	suspend fun googleLogin(idToken: String): Response<LoginResponse> {
		return RetrofitClient.authApi(context).googleLogin(
			GoogleLoginRequest(idToken.trim())
		)
	}

	suspend fun forgotPassword(email: String): Response<BasicMessageResponse> {
		return RetrofitClient.authApi(context).forgotPassword(
			ForgotPasswordRequest(email.trim().lowercase())
		)
	}

	suspend fun verifyResetCode(email: String, code: String): Response<BasicMessageResponse> {
		return RetrofitClient.authApi(context).verifyResetCode(
			VerifyResetCodeRequest(
				email = email.trim().lowercase(),
				code = code.trim()
			)
		)
	}

	suspend fun resetPassword(
		email: String,
		code: String,
		newPassword: String
	): Response<BasicMessageResponse> {
		return RetrofitClient.authApi(context).resetPassword(
			ResetPasswordRequest(
				email = email.trim().lowercase(),
				code = code.trim(),
				newPassword = newPassword
			)
		)
	}
}

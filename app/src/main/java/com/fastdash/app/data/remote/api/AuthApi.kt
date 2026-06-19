package com.fastdash.app.data.remote.api

import com.fastdash.app.data.model.request.ForgotPasswordRequest
import com.fastdash.app.data.model.request.GoogleLoginRequest
import com.fastdash.app.data.model.request.LoginRequest
import com.fastdash.app.data.model.request.ResetPasswordRequest
import com.fastdash.app.data.model.request.RegisterRequest
import com.fastdash.app.data.model.request.VerifyResetCodeRequest
import com.fastdash.app.data.model.response.BasicMessageResponse
import com.fastdash.app.data.model.response.LoginResponse
import com.fastdash.app.data.model.response.RegisterResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

	@POST("api/v1/auth/login")
	suspend fun login(
		@Body request: LoginRequest
	): Response<LoginResponse>

	@POST("api/v1/auth/register")
	suspend fun register(
		@Body request: RegisterRequest
	): Response<RegisterResponse>

	@POST("api/v1/auth/google")
	suspend fun googleLogin(
		@Body request: GoogleLoginRequest
	): Response<LoginResponse>

	@POST("api/v1/auth/forgot-password")
	suspend fun forgotPassword(
		@Body request: ForgotPasswordRequest
	): Response<BasicMessageResponse>

	@POST("api/v1/auth/verify-reset-code")
	suspend fun verifyResetCode(
		@Body request: VerifyResetCodeRequest
	): Response<BasicMessageResponse>

	@POST("api/v1/auth/reset-password")
	suspend fun resetPassword(
		@Body request: ResetPasswordRequest
	): Response<BasicMessageResponse>
}

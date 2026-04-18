package com.fastdash.app.data.repository


import com.fastdash.app.data.model.request.LoginRequest
import com.fastdash.app.data.model.response.LoginResponse
import com.fastdash.app.data.remote.retrofit.RetrofitClient
import retrofit2.Response

class AuthRepository {

    suspend fun login(email: String, password: String): Response<LoginResponse> {
        return RetrofitClient.authApi.login(
            LoginRequest(email = email, password = password)
        )
    }
}
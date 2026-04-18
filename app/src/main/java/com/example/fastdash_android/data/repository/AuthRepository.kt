package com.example.fastdash_android.data.repository


import com.example.fastdash_android.data.model.request.LoginRequest
import com.example.fastdash_android.data.model.response.LoginResponse
import com.example.fastdash_android.data.remote.retrofit.RetrofitClient
import retrofit2.Response

class AuthRepository {

    suspend fun login(email: String, password: String): Response<LoginResponse> {
        return RetrofitClient.authApi.login(
            LoginRequest(email = email, password = password)
        )
    }
}
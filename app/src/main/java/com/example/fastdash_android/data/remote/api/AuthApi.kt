package com.example.fastdash_android.data.remote.api


import com.example.fastdash_android.data.model.request.LoginRequest
import com.example.fastdash_android.data.model.response.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("api/v1/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>
}
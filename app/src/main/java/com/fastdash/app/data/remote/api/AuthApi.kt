package com.fastdash.app.data.remote.api


import com.fastdash.app.data.model.request.LoginRequest
import com.fastdash.app.data.model.response.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("api/v1/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>
}
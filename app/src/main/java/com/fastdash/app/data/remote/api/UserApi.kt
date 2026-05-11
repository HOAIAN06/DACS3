package com.fastdash.app.data.remote.api

import com.fastdash.app.data.model.response.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface UserApi {
    @GET("api/v1/users/me")
    suspend fun getMe(): Response<UserResponse>

    @PUT("api/v1/users/me")
    suspend fun updateMe(@Body request: UserResponse): Response<UserResponse>
}

package com.fastdash.app.data.repository

import android.content.Context
import com.fastdash.app.data.model.request.UpdateProfileRequest
import com.fastdash.app.data.model.response.UserResponse
import com.fastdash.app.data.remote.retrofit.RetrofitClient
import retrofit2.Response

class UserRepository(private val context: Context) {
    private val api = RetrofitClient.userApi(context)

    suspend fun getMe(): Response<UserResponse> = api.getMe()

    suspend fun updateMe(request: UpdateProfileRequest): Response<UserResponse> = api.updateMe(request)
}

package com.fastdash.app.data.remote.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

interface AdminUserApi {
    @GET("api/v1/admin/users")
    suspend fun getUsers(): Response<List<UserResponse>>

    @GET("api/v1/admin/users/{id}")
    suspend fun getUserDetail(@Path("id") id: Long): Response<UserResponse>

    @PATCH("api/v1/admin/users/{id}/status")
    suspend fun updateUserStatus(
        @Path("id") id: Long,
        @retrofit2.http.Body request: UpdateUserStatusRequest
    ): Response<UserResponse>

    @PATCH("api/v1/admin/users/{id}/role")
    suspend fun updateUserRole(
        @Path("id") id: Long,
        @retrofit2.http.Body request: UpdateUserRoleRequest
    ): Response<UserResponse>
}

data class UserResponse(
    val id: Long,
    val fullName: String,
    val email: String,
    val phone: String,
    val role: String,
    val status: Int,
    val createdAt: String
)

data class UpdateUserStatusRequest(
    val status: Int
)

data class UpdateUserRoleRequest(
    val role: String
)

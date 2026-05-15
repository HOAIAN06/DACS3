package com.fastdash.app.data.remote.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface AdminBranchApi {
    @GET("api/v1/admin/branches")
    suspend fun getBranches(): Response<List<BranchResponse>>

    @POST("api/v1/admin/branches")
    suspend fun createBranch(@Body request: CreateBranchRequest): Response<BranchResponse>

    @PUT("api/v1/admin/branches/{id}")
    suspend fun updateBranch(
        @Path("id") id: Long,
        @Body request: CreateBranchRequest
    ): Response<BranchResponse>

    @DELETE("api/v1/admin/branches/{id}")
    suspend fun deleteBranch(@Path("id") id: Long): Response<Void>

     @PATCH("api/v1/admin/branches/{id}/status")
     suspend fun updateBranchStatus(
         @Path("id") id: Long,
         @Query("status") status: Int
     ): Response<BranchResponse>
}

data class BranchResponse(
    val id: Long,
    val name: String,
    val address: String,
    val phone: String,
    val openTime: String? = null,
    val closeTime: String? = null,
    val status: Int,
    val createdAt: String
)

data class CreateBranchRequest(
    val name: String,
    val address: String,
    val phone: String,
    val openTime: String? = null,
    val closeTime: String? = null
)

data class UpdateStatusRequest(
    val status: Int
)

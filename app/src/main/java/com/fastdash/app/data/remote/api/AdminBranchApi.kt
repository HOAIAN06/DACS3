package com.fastdash.app.data.remote.api

import com.fastdash.app.data.model.response.BranchResponse
import com.google.gson.annotations.SerializedName
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

data class CreateBranchRequest(
    @SerializedName("name")
    val name: String,
    @SerializedName("address")
    val address: String,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("openTime")
    val openTime: String? = null,
    @SerializedName("closeTime")
    val closeTime: String? = null,
    @SerializedName("latitude")
    val latitude: Double? = null,
    @SerializedName("longitude")
    val longitude: Double? = null,
    @SerializedName("status")
    val status: Int = 1
)

data class UpdateStatusRequest(
    val status: Int
)

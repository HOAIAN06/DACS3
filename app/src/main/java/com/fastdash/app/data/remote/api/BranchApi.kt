package com.fastdash.app.data.remote.api

import com.fastdash.app.data.model.response.BranchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface BranchApi {
    @GET("api/v1/branches")
    suspend fun getBranches(): Response<List<BranchResponse>>

    @GET("api/v1/branches/{id}")
    suspend fun getBranchDetail(@Path("id") id: Long): Response<BranchResponse>
}

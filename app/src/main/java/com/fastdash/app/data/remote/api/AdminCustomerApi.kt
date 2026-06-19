package com.fastdash.app.data.remote.api

import com.fastdash.app.data.model.request.UpdateProfileRequest
import com.fastdash.app.data.model.response.AdminCustomerOrdersPageResponse
import com.fastdash.app.data.model.response.AdminCustomerResponse
import com.fastdash.app.data.model.response.AdminCustomersPageResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.PUT
import retrofit2.http.Query

interface AdminCustomerApi {
    @GET("api/v1/admin/customers")
    suspend fun getCustomers(
        @Query("keyword") keyword: String? = null,
        @Query("status") status: Int? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
        @Query("sortBy") sortBy: String = "createdAt",
        @Query("sortDir") sortDir: String = "desc"
    ): Response<AdminCustomersPageResponse>

    @GET("api/v1/admin/customers/{id}")
    suspend fun getCustomerDetail(@Path("id") id: Long): Response<AdminCustomerResponse>

    @PUT("api/v1/admin/customers/{id}")
    suspend fun updateCustomer(
        @Path("id") id: Long,
        @Body request: UpdateProfileRequest
    ): Response<AdminCustomerResponse>

    @PATCH("api/v1/admin/customers/{id}/status")
    suspend fun updateCustomerStatus(
        @Path("id") id: Long,
        @Query("status") status: Int
    ): Response<AdminCustomerResponse>

    @GET("api/v1/admin/customers/{id}/orders")
    suspend fun getCustomerOrders(
        @Path("id") id: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<AdminCustomerOrdersPageResponse>
}

package com.fastdash.app.data.remote.api

import com.fastdash.app.data.model.response.ProductResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface AdminProductApi {

    @Multipart
    @POST("/api/v1/admin/products")
    suspend fun createProduct(
        @Part("name") name: RequestBody,
        @Part("description") description: RequestBody,
        @Part("basePrice") basePrice: RequestBody,
        @Part("categoryId") categoryId: RequestBody,
        @Part("isCustomizable") isCustomizable: RequestBody,
        @Part("status") status: RequestBody,
        @Part image: MultipartBody.Part
    ): Response<ProductResponse>

    @Multipart
    @PUT("/api/v1/admin/products/{id}")
    suspend fun updateProduct(
        @Path("id") id: Long,
        @Part("name") name: RequestBody,
        @Part("description") description: RequestBody,
        @Part("basePrice") basePrice: RequestBody,
        @Part("categoryId") categoryId: RequestBody,
        @Part("isCustomizable") isCustomizable: RequestBody,
        @Part("status") status: RequestBody,
        @Part image: MultipartBody.Part?
    ): Response<ProductResponse>
}

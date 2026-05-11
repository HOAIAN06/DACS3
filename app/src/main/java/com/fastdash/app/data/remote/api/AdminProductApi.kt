package com.fastdash.app.data.remote.api

import com.fastdash.app.data.model.request.AdminProductRequest
import com.fastdash.app.data.model.response.ProductResponse
import com.fastdash.app.data.model.response.UploadImageResponse
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface AdminProductApi {

    @Multipart
    @POST("/api/v1/admin/uploads/image")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part
    ): UploadImageResponse

    @POST("/api/v1/admin/products")
    suspend fun createProduct(
        @Body request: AdminProductRequest
    ): ProductResponse
}
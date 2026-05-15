package com.fastdash.app.data.repository

import android.content.Context
import com.fastdash.app.data.model.request.AdminProductRequest
import com.fastdash.app.data.model.response.ProductResponse
import com.fastdash.app.data.model.response.UploadImageResponse
import com.fastdash.app.data.remote.api.UpdateProductRequest
import com.fastdash.app.data.remote.retrofit.RetrofitClient
import okhttp3.MultipartBody
import retrofit2.Response

class AdminProductRepository(context: Context) {

    private val api = RetrofitClient.adminProductApi(context)
    private val apiExtended = RetrofitClient.adminProductApiExtended(context)

    suspend fun uploadImage(file: MultipartBody.Part): Response<UploadImageResponse> =
        api.uploadImage(file)

    suspend fun createProduct(request: AdminProductRequest): Response<ProductResponse> =
        api.createProduct(request)

    suspend fun getProducts() = apiExtended.getProducts()

    suspend fun getProductDetail(id: Long) = apiExtended.getProductDetail(id)

    suspend fun updateProduct(id: Long, request: UpdateProductRequest) =
        apiExtended.updateProduct(id, request)

    suspend fun deleteProduct(id: Long) = apiExtended.deleteProduct(id)

     suspend fun updateProductStatus(id: Long, status: Int) =
         apiExtended.updateProductStatus(id, status)

    suspend fun addToppingToProduct(productId: Long, toppingId: Long) =
        apiExtended.addToppingToProduct(productId, toppingId)

    suspend fun removeToppingFromProduct(productId: Long, toppingId: Long) =
        apiExtended.removeToppingFromProduct(productId, toppingId)
}
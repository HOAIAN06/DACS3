package com.fastdash.app.data.repository

import android.content.Context
import com.fastdash.app.data.model.response.ProductResponse
import com.fastdash.app.data.remote.retrofit.RetrofitClient
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response

class AdminProductRepository(context: Context) {

    private val api = RetrofitClient.adminProductApi(context)
    private val apiExtended = RetrofitClient.adminProductApiExtended(context)

    suspend fun createProduct(
        name: RequestBody,
        description: RequestBody,
        basePrice: RequestBody,
        categoryId: RequestBody,
        isCustomizable: RequestBody,
        status: RequestBody,
        image: MultipartBody.Part
    ): Response<ProductResponse> = api.createProduct(
        name = name,
        description = description,
        basePrice = basePrice,
        categoryId = categoryId,
        isCustomizable = isCustomizable,
        status = status,
        image = image
    )

    suspend fun getProducts() = apiExtended.getProducts()

    suspend fun getProductDetail(id: Long) = apiExtended.getProductDetail(id)

    suspend fun updateProduct(
        id: Long,
        name: RequestBody,
        description: RequestBody,
        basePrice: RequestBody,
        categoryId: RequestBody,
        isCustomizable: RequestBody,
        status: RequestBody,
        image: MultipartBody.Part?
    ): Response<ProductResponse> = api.updateProduct(
        id = id,
        name = name,
        description = description,
        basePrice = basePrice,
        categoryId = categoryId,
        isCustomizable = isCustomizable,
        status = status,
        image = image
    )

    suspend fun deleteProduct(id: Long) = apiExtended.deleteProduct(id)

     suspend fun updateProductStatus(id: Long, status: Int) =
         apiExtended.updateProductStatus(id, status)

    suspend fun addToppingToProduct(productId: Long, toppingId: Long) =
        apiExtended.addToppingToProduct(productId, toppingId)

    suspend fun removeToppingFromProduct(productId: Long, toppingId: Long) =
        apiExtended.removeToppingFromProduct(productId, toppingId)
}

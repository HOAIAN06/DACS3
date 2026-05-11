package com.fastdash.app.data.repository

import android.content.Context
import com.fastdash.app.data.model.response.ProductResponse
import com.fastdash.app.data.remote.retrofit.RetrofitClient
import retrofit2.Response

class ProductRepository(private val context: Context) {

    suspend fun getProducts(categoryId: Long? = null): Response<List<ProductResponse>> {
        return RetrofitClient.productApi(context).getProducts(categoryId)
    }

    suspend fun getProductById(id: Long) = RetrofitClient.productApi(context).getProductById(id)

    suspend fun getProductToppings(id: Long) = RetrofitClient.productApi(context).getProductToppings(id)

    suspend fun getProductSizes(id: Long) = RetrofitClient.productApi(context).getProductSizes(id)
}

package com.fastdash.app.data.remote.api

import com.fastdash.app.data.model.response.ProductResponse
import com.fastdash.app.data.model.response.ProductSizeResponse
import com.fastdash.app.data.model.response.ToppingResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ProductApi {

    @GET("api/v1/products")
    suspend fun getProducts(
        @Query("categoryId") categoryId: Long? = null
    ): Response<List<ProductResponse>>

    @GET("api/v1/products/{id}")
    suspend fun getProductById(@Path("id") id: Long): Response<ProductResponse>

    @GET("api/v1/products/{id}/toppings")
    suspend fun getProductToppings(@Path("id") id: Long): Response<List<ToppingResponse>>

    @GET("api/v1/products/{id}/sizes")
    suspend fun getProductSizes(@Path("id") id: Long): Response<List<ProductSizeResponse>>
}

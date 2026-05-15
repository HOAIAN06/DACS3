package com.fastdash.app.data.remote.api

import com.fastdash.app.data.model.response.AdminDashboardSummaryResponse
import com.fastdash.app.data.model.response.AdminOrderSummaryResponse
import com.fastdash.app.data.model.response.AdminPageResponse
import com.fastdash.app.data.model.response.OrderResponse
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

interface AdminToppingApi {
    @GET("api/v1/admin/toppings")
    suspend fun getToppings(): Response<List<AdminToppingResponse>>

    @POST("api/v1/admin/toppings")
    suspend fun createTopping(@Body request: CreateToppingRequest): Response<AdminToppingResponse>

    @PUT("api/v1/admin/toppings/{id}")
    suspend fun updateTopping(
        @Path("id") id: Long,
        @Body request: CreateToppingRequest
    ): Response<AdminToppingResponse>

    @DELETE("api/v1/admin/toppings/{id}")
    suspend fun deleteTopping(@Path("id") id: Long): Response<Void>

     @PATCH("api/v1/admin/toppings/{id}/status")
     suspend fun updateToppingStatus(
         @Path("id") id: Long,
         @Query("status") status: Int
     ): Response<AdminToppingResponse>
}

data class AdminToppingResponse(
    val id: Long,
    val name: String? = null,
    val price: Double,
    @SerializedName(value = "imageUrl", alternate = ["image_url", "url", "secure_url"]) val imageUrl: String? = null,
    val status: Int,
    val createdAt: String? = null
)

data class CreateToppingRequest(
    val name: String,
    val price: Double,
    val imageUrl: String? = null
)

interface AdminSizeApi {
    @GET("api/v1/admin/products/{productId}/sizes")
    suspend fun getSizesByProduct(@Path("productId") productId: Long): Response<List<AdminSizeResponse>>

    @POST("api/v1/admin/products/{productId}/sizes")
    suspend fun createSize(
        @Path("productId") productId: Long,
        @Body request: CreateSizeRequest
    ): Response<AdminSizeResponse>

    @PUT("api/v1/admin/products/sizes/{sizeId}")
    suspend fun updateSize(
        @Path("sizeId") sizeId: Long,
        @Body request: CreateSizeRequest
    ): Response<AdminSizeResponse>

    @DELETE("api/v1/admin/products/sizes/{sizeId}")
    suspend fun deleteSize(@Path("sizeId") sizeId: Long): Response<Void>

     @PATCH("api/v1/admin/products/sizes/{sizeId}/status")
     suspend fun updateSizeStatus(
         @Path("sizeId") sizeId: Long,
         @Query("status") status: Int
     ): Response<AdminSizeResponse>
}

data class AdminSizeResponse(
    val id: Long,
    val productId: Long,
    val sizeName: String,
    val price: Double,
    val status: Int
)

data class CreateSizeRequest(
    @SerializedName("sizeName")
    val sizeName: String,
    val price: Double
)

interface AdminPaymentApi {
    @GET("api/v1/admin/payments")
    suspend fun getPayments(): Response<List<AdminPaymentResponse>>

    @GET("api/v1/admin/payments/{id}")
    suspend fun getPaymentDetail(@Path("id") id: Long): Response<AdminPaymentResponse>

    @PATCH("api/v1/admin/payments/{id}/status")
    suspend fun updatePaymentStatus(
        @Path("id") id: Long,
        @Query("status") status: String
    ): Response<AdminPaymentResponse>
}

data class AdminPaymentResponse(
    val id: Long,
    val orderId: Long,
    val amount: Double,
    val method: String,
    val status: String,
    val transactionCode: String? = null,
    val paidAt: String? = null,
    val createdAt: String
)

interface AdminOrderStatusApi {
     @PUT("api/v1/admin/orders/{id}/status")
     suspend fun updateOrderStatus(
         @Path("id") id: Long,
         @Body request: UpdateOrderStatusRequest
     ): Response<OrderResponse>
}

interface AdminOrderApi {
    @GET("api/v1/admin/orders")
    suspend fun getOrders(
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
        @Query("status") status: String? = null,
        @Query("keyword") keyword: String? = null,
        @Query("fromDate") fromDate: String? = null,
        @Query("toDate") toDate: String? = null
    ): Response<AdminPageResponse<AdminOrderSummaryResponse>>

    @GET("api/v1/admin/orders/{id}")
    suspend fun getOrderDetail(@Path("id") id: Long): Response<OrderResponse>
}

interface AdminDashboardApi {
    @GET("api/v1/admin/dashboard/summary")
    suspend fun getSummary(): Response<AdminDashboardSummaryResponse>
}

data class UpdateOrderStatusRequest(
     val status: String
)

interface AdminCategoryApi {
     @GET("api/v1/admin/categories")
     suspend fun getCategories(): Response<List<AdminCategoryResponse>>

     @POST("api/v1/admin/categories")
     suspend fun createCategory(@Body request: CreateCategoryRequest): Response<AdminCategoryResponse>

     @PUT("api/v1/admin/categories/{id}")
     suspend fun updateCategory(
         @Path("id") id: Long,
         @Body request: CreateCategoryRequest
     ): Response<AdminCategoryResponse>

     @DELETE("api/v1/admin/categories/{id}")
     suspend fun deleteCategory(@Path("id") id: Long): Response<Void>

      @PATCH("api/v1/admin/categories/{id}/status")
      suspend fun updateCategoryStatus(
          @Path("id") id: Long,
          @Query("status") status: Int
      ): Response<AdminCategoryResponse>
}

data class AdminCategoryResponse(
     val id: Long,
     val name: String? = null,
     val description: String? = null,
     val status: Int,
     val createdAt: String? = null
)

data class CreateCategoryRequest(
     val name: String,
     val description: String? = null
)

interface AdminProductApiExtended {
     @GET("api/v1/admin/products")
     suspend fun getProducts(): Response<List<AdminProductResponse>>

     @GET("api/v1/admin/products/{id}")
     suspend fun getProductDetail(@Path("id") id: Long): Response<AdminProductResponse>

     @PUT("api/v1/admin/products/{id}")
     suspend fun updateProduct(
         @Path("id") id: Long,
         @Body request: UpdateProductRequest
     ): Response<AdminProductResponse>

     @DELETE("api/v1/admin/products/{id}")
     suspend fun deleteProduct(@Path("id") id: Long): Response<Void>

      @PATCH("api/v1/admin/products/{id}/status")
      suspend fun updateProductStatus(
          @Path("id") id: Long,
          @Query("status") status: Int
      ): Response<AdminProductResponse>

     @POST("api/v1/admin/products/{productId}/toppings/{toppingId}")
     suspend fun addToppingToProduct(
         @Path("productId") productId: Long,
         @Path("toppingId") toppingId: Long
     ): Response<Void>

     @DELETE("api/v1/admin/products/{productId}/toppings/{toppingId}")
     suspend fun removeToppingFromProduct(
         @Path("productId") productId: Long,
         @Path("toppingId") toppingId: Long
     ): Response<Void>
}

data class AdminProductResponse(
     val id: Long,
     val categoryId: Long,
     val name: String? = null,
     val description: String? = null,
     @SerializedName(value = "basePrice", alternate = ["base_price"]) val basePrice: Double,
     @SerializedName(
         value = "imageUrl",
         alternate = [
             "image_url",
             "imagePath",
             "image_path",
             "image",
             "productImageUrl",
             "product_image_url",
             "thumbnailUrl",
             "thumbnail_url",
             "url",
             "secure_url",
             "photoUrl",
             "photo_url"
         ]
     )
      val imageUrl: String? = null,
     val isCustomizable: Int,
     val status: Int,
     val createdAt: String? = null
)

data class UpdateProductRequest(
     @SerializedName("categoryId")
     val categoryId: Long,
     val name: String,
     val description: String,
     @SerializedName("basePrice")
     val basePrice: Double,
     @SerializedName("imageUrl")
     val imageUrl: String,
     @SerializedName("isCustomizable")
     val isCustomizable: Int
)



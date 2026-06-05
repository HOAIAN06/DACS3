package com.fastdash.app.data.remote.api

import com.fastdash.app.data.model.response.AdminDashboardSummaryResponse
import com.fastdash.app.data.model.response.AdminOrderDetailResponse
import com.fastdash.app.data.model.response.AdminOrdersPageResponse
import com.fastdash.app.data.model.response.AdminOrderSummaryResponse
import com.fastdash.app.data.model.response.AdminPageResponse
import com.fastdash.app.data.model.response.RevenueReportResponse
import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface AdminToppingApi {
    @GET("api/v1/admin/toppings")
    suspend fun getToppings(): Response<List<AdminToppingResponse>>

    @Multipart
    @POST("api/v1/admin/toppings")
    suspend fun createTopping(
        @Part("name") name: RequestBody,
        @Part("price") price: RequestBody,
        @Part("status") status: RequestBody,
        @Part image: MultipartBody.Part
    ): Response<AdminToppingResponse>

    @Multipart
    @PUT("api/v1/admin/toppings/{id}")
    suspend fun updateTopping(
        @Path("id") id: Long,
        @Part("name") name: RequestBody,
        @Part("price") price: RequestBody,
        @Part("status") status: RequestBody,
        @Part image: MultipartBody.Part?
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
    @SerializedName("imageUrl")
    val imageUrl: String? = null,
    val status: Int = 1
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
    val id: Long = 0L,
    val orderId: Long = 0L,
    val amount: Double = 0.0,
    val method: String? = null,
    val status: String? = null,
    val transactionCode: String? = null,
    val paidAt: String? = null,
    val createdAt: String? = null
)

interface AdminOrderStatusApi {
     @PUT("api/v1/admin/orders/{id}/status")
     suspend fun updateOrderStatus(
         @Path("id") id: Long,
         @Body request: UpdateOrderStatusRequest
     ): Response<AdminOrderDetailResponse>
}

interface AdminOrderApi {
    @GET("api/v1/admin/orders")
    suspend fun getAdminOrders(
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
        @Query("status") status: String? = null,
        @Query("keyword") keyword: String? = null,
        @Query("fromDate") fromDate: String? = null,
        @Query("toDate") toDate: String? = null,
        @Query("sort") sort: String? = "createdAt,desc"
    ): Response<AdminOrdersPageResponse>

    @GET("api/v1/admin/orders/{id}")
    suspend fun getAdminOrderDetail(@Path("id") id: Long): Response<AdminOrderDetailResponse>

    @PUT("api/v1/admin/orders/{id}/status")
    suspend fun updateAdminOrderStatus(
        @Path("id") id: Long,
        @Body request: UpdateOrderStatusRequest
    ): Response<AdminOrderDetailResponse>
}

interface AdminDashboardApi {
    @GET("api/v1/admin/dashboard/summary")
    suspend fun getAdminDashboardSummary(): Response<AdminDashboardSummaryResponse>

    @GET("api/v1/admin/revenue-report")
    suspend fun getRevenueReport(
        @Query("period") period: String? = null,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null
    ): Response<RevenueReportResponse>
}

data class UpdateOrderStatusRequest(
     val status: String
)

interface AdminCategoryApi {
     @GET("api/v1/admin/categories")
     suspend fun getCategories(): Response<List<AdminCategoryResponse>>

     @Multipart
     @POST("api/v1/admin/categories")
     suspend fun createCategory(
         @Part("name") name: RequestBody,
         @Part("description") description: RequestBody,
         @Part("status") status: RequestBody,
         @Part image: MultipartBody.Part
     ): Response<AdminCategoryResponse>

     @Multipart
     @PUT("api/v1/admin/categories/{id}")
     suspend fun updateCategory(
         @Path("id") id: Long,
         @Part("name") name: RequestBody,
         @Part("description") description: RequestBody,
         @Part("status") status: RequestBody,
         @Part image: MultipartBody.Part?
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
     @SerializedName(value = "imageUrl", alternate = ["image_url", "url", "secure_url"]) val imageUrl: String? = null,
     val status: Int,
     val createdAt: String? = null
)

data class CreateCategoryRequest(
     val name: String,
     val description: String? = null,
     val status: Int = 1
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
     @SerializedName(value = "categoryId", alternate = ["category_id"])
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



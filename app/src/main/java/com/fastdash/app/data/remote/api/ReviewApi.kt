package com.fastdash.app.data.remote.api

import com.fastdash.app.data.model.request.ReviewRequest
import com.fastdash.app.data.model.response.ApiResponse
import com.fastdash.app.data.model.response.ReviewableItemResponse
import com.fastdash.app.data.model.response.ReviewResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ReviewApi {
    @GET("api/v1/reviews/products/{productId}")
    suspend fun getProductReviews(
        @Path("productId") productId: Long
    ): Response<ResponseBody>

    @GET("api/v1/reviews/orders/{orderId}/reviewable-items")
    suspend fun getReviewableItems(
        @Path("orderId") orderId: Long
    ): Response<ApiResponse<List<ReviewableItemResponse>>>

    @POST("api/v1/reviews")
    suspend fun submitReview(@Body request: ReviewRequest): Response<ApiResponse<ReviewResponse>>

    @GET("api/v1/reviews/my")
    suspend fun getMyReviews(): Response<ResponseBody>
}

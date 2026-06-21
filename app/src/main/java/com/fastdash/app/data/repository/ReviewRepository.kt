package com.fastdash.app.data.repository

import android.content.Context
import com.fastdash.app.data.model.request.ReviewRequest
import com.fastdash.app.data.model.response.ApiResponse
import com.fastdash.app.data.model.response.ReviewableItemResponse
import com.fastdash.app.data.model.response.ReviewResponse
import com.fastdash.app.data.remote.retrofit.RetrofitClient
import okhttp3.ResponseBody
import retrofit2.Response

class ReviewRepository(private val context: Context) {
    private val api = RetrofitClient.reviewApi(context)

    suspend fun getProductReviews(productId: Long): Response<ResponseBody> {
        return api.getProductReviews(productId)
    }

    suspend fun getReviewableItems(orderId: Long): Response<ApiResponse<List<ReviewableItemResponse>>> {
        return api.getReviewableItems(orderId)
    }

    suspend fun submitReview(request: ReviewRequest): Response<ApiResponse<ReviewResponse>> {
        return api.submitReview(request)
    }

    suspend fun getMyReviews(): Response<ResponseBody> {
        return api.getMyReviews()
    }
}

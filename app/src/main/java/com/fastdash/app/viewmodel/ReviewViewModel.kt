package com.fastdash.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fastdash.app.data.model.request.ReviewRequest
import com.fastdash.app.data.model.response.ApiResponse
import com.fastdash.app.data.model.response.ApiErrorResponse
import com.fastdash.app.data.model.response.ReviewResponse
import com.fastdash.app.data.model.response.ReviewableItemResponse
import com.fastdash.app.data.repository.ReviewRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Response

data class ReviewUiState(
    val reviews: List<ReviewResponse> = emptyList(),
    val myReviews: List<ReviewResponse> = emptyList(),
    val reviewableItems: List<ReviewableItemResponse> = emptyList(),
    val loadingProductReviews: Boolean = false,
    val loadingReviewableItems: Boolean = false,
    val loadingMyReviews: Boolean = false,
    val reviewableItemsLoadedSuccessfully: Boolean = false,
    val reviewableItemsErrorMessage: String? = null,
    val submitting: Boolean = false,
    val submitSuccess: Boolean = false,
    val submitMessage: String? = null,
    val errorMessage: String? = null
)

class ReviewViewModel(private val repository: ReviewRepository) : ViewModel() {
    private val gson = Gson()

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    fun loadReviews(productId: Long) {
        if (productId <= 0L) {
            _uiState.update { it.copy(reviews = emptyList(), errorMessage = "Không tìm thấy món ăn để tải đánh giá") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(loadingProductReviews = true, errorMessage = null) }
            try {
                val response = repository.getProductReviews(productId)
                if (response.isSuccessful) {
                    val parsedReviews = parseReviewListResponse(response.body())
                    _uiState.update {
                        it.copy(
                            reviews = parsedReviews,
                            loadingProductReviews = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            loadingProductReviews = false,
                            errorMessage = parseErrorMessage(response) ?: "Không thể tải danh sách đánh giá"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        loadingProductReviews = false,
                        errorMessage = e.message ?: "Không thể tải danh sách đánh giá"
                    )
                }
            }
        }
    }

    fun loadMyReviews() {
        viewModelScope.launch {
            _uiState.update { it.copy(loadingMyReviews = true, errorMessage = null) }
            try {
                val response = repository.getMyReviews()
                if (response.isSuccessful) {
                    val parsedReviews = parseReviewListResponse(response.body())
                    _uiState.update {
                        it.copy(
                            myReviews = parsedReviews,
                            loadingMyReviews = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            loadingMyReviews = false,
                            errorMessage = parseErrorMessage(response) ?: "Không thể tải danh sách đánh giá của bạn"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        loadingMyReviews = false,
                        errorMessage = e.message ?: "Không thể tải danh sách đánh giá của bạn"
                    )
                }
            }
        }
    }

    fun loadReviewableItems(orderId: Long) {
        if (orderId <= 0L) {
            _uiState.update { it.copy(reviewableItems = emptyList(), errorMessage = "Không tìm thấy đơn hàng để đánh giá") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    loadingReviewableItems = true,
                    reviewableItems = emptyList(),
                    reviewableItemsLoadedSuccessfully = false,
                    reviewableItemsErrorMessage = null,
                    errorMessage = null
                )
            }
            try {
                val reviewableResponse = repository.getReviewableItems(orderId)
                val myReviewsResponse = repository.getMyReviews()
                val parsedMyReviews = if (myReviewsResponse.isSuccessful) parseReviewListResponse(myReviewsResponse.body()) else _uiState.value.myReviews

                if (reviewableResponse.isSuccessful) {
                    _uiState.update {
                        it.copy(
                            reviewableItems = reviewableResponse.body()?.data.orEmpty(),
                            myReviews = parsedMyReviews,
                            loadingReviewableItems = false,
                            reviewableItemsLoadedSuccessfully = true,
                            reviewableItemsErrorMessage = null
                        )
                    }
                } else {
                    val message = parseErrorMessage(reviewableResponse) ?: "Không thể tải trạng thái đánh giá"
                    _uiState.update {
                        it.copy(
                            loadingReviewableItems = false,
                            reviewableItemsLoadedSuccessfully = false,
                            reviewableItemsErrorMessage = message,
                            errorMessage = message
                        )
                    }
                }
            } catch (e: Exception) {
                val message = e.message ?: "Không thể tải trạng thái đánh giá"
                _uiState.update {
                    it.copy(
                        loadingReviewableItems = false,
                        reviewableItemsLoadedSuccessfully = false,
                        reviewableItemsErrorMessage = message,
                        errorMessage = message
                    )
                }
            }
        }
    }

    fun submitReview(orderId: Long, productId: Long, rating: Int, comment: String) {
        val trimmedComment = comment.trim().ifBlank { null }
        when {
            orderId <= 0L -> {
                _uiState.update { it.copy(errorMessage = "Đơn hàng không hợp lệ") }
                return
            }

            productId <= 0L -> {
                _uiState.update { it.copy(errorMessage = "Món ăn không hợp lệ") }
                return
            }

            rating !in 1..5 -> {
                _uiState.update { it.copy(errorMessage = "Vui lòng chọn số sao từ 1 đến 5") }
                return
            }

            trimmedComment != null && trimmedComment.length > 1000 -> {
                _uiState.update { it.copy(errorMessage = "Bình luận không được vượt quá 1000 ký tự") }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    submitting = true,
                    submitSuccess = false,
                    submitMessage = null,
                    errorMessage = null
                )
            }
            try {
                val response = repository.submitReview(
                    ReviewRequest(
                        orderId = orderId,
                        productId = productId,
                        rating = rating,
                        comment = trimmedComment
                    )
                )
                if (response.isSuccessful) {
                    val savedReview = response.body()?.data
                    _uiState.update { state ->
                        state.copy(
                            reviewableItems = state.reviewableItems.map { item ->
                                if (item.productId == productId && (item.orderId == null || item.orderId == orderId)) {
                                    item.copy(reviewed = true, review = savedReview ?: item.review)
                                } else {
                                    item
                                }
                            },
                            myReviews = mergeReview(state.myReviews, savedReview),
                            submitting = false,
                            submitSuccess = true,
                            submitMessage = response.body()?.message ?: "Cảm ơn bạn đã đánh giá món ăn"
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            submitting = false,
                            errorMessage = parseErrorMessage(response) ?: "Gửi đánh giá thất bại"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        submitting = false,
                        errorMessage = e.message ?: "Gửi đánh giá thất bại"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun consumeSubmitSuccess() {
        _uiState.update { it.copy(submitSuccess = false, submitMessage = null) }
    }

    private fun mergeReview(current: List<ReviewResponse>, savedReview: ReviewResponse?): List<ReviewResponse> {
        if (savedReview == null) return current
        val matchedIndex = current.indexOfFirst { review ->
            review.id == savedReview.id ||
                (review.orderId == savedReview.orderId && review.productId == savedReview.productId)
        }
        if (matchedIndex == -1) {
            return listOf(savedReview) + current
        }

        return current.toMutableList().apply {
            this[matchedIndex] = savedReview
        }
    }

    private fun <T> parseErrorMessage(response: Response<T>): String? {
        val rawError = response.errorBody()?.string().orEmpty()
        if (rawError.isBlank()) return null
        return runCatching {
            gson.fromJson(rawError, ApiErrorResponse::class.java)?.displayMessage()
        }.getOrNull() ?: rawError
    }

    private fun parseReviewListResponse(body: ResponseBody?): List<ReviewResponse> {
        val json = body?.string().orEmpty()
        if (json.isBlank()) return emptyList()

        val wrapperType = object : TypeToken<ApiResponse<List<ReviewResponse>>>() {}.type
        val listType = object : TypeToken<List<ReviewResponse>>() {}.type

        return runCatching {
            gson.fromJson<ApiResponse<List<ReviewResponse>>>(json, wrapperType)?.data
        }.getOrNull()
            ?: runCatching {
                gson.fromJson<List<ReviewResponse>>(json, listType)
            }.getOrNull()
            ?: emptyList()
    }
}

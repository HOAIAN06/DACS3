package com.fastdash.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fastdash.app.data.model.response.ApiErrorResponse
import com.fastdash.app.data.model.response.CartResponse
import com.fastdash.app.data.repository.CartRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Response

class CartViewModel(private val repository: CartRepository) : ViewModel() {
    private val gson = Gson()

    private val _cart = MutableStateFlow<CartResponse?>(null)
    val cart: StateFlow<CartResponse?> = _cart.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun loadCart() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = repository.getCart()
                if (response.isSuccessful) {
                    _cart.value = response.body()
                } else {
                    _message.value = buildErrorMessage("Không thể tải giỏ hàng", response)
                }
            } catch (e: Exception) {
                _message.value = "Lỗi kết nối: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun addToCart(productId: Long, quantity: Int, productSizeId: Long?, toppingIds: List<Long>) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = repository.addToCart(productId, quantity, productSizeId, toppingIds)
                if (response.isSuccessful) {
                    _cart.value = response.body()
                    _message.value = "Đã thêm vào giỏ hàng"
                } else {
                    _message.value = buildErrorMessage("Thêm vào giỏ thất bại", response)
                }
            } catch (e: Exception) {
                _message.value = "Lỗi: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun updateCartItem(itemId: Long, quantity: Int, note: String? = null) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = repository.updateCartItem(itemId, quantity, note)
                if (response.isSuccessful) {
                    _cart.value = response.body()
                    _message.value = "Đã cập nhật giỏ hàng"
                } else {
                    _message.value = buildErrorMessage("Cập nhật giỏ hàng thất bại", response)
                }
            } catch (e: Exception) {
                _message.value = "Lỗi: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun removeFromCart(itemId: Long) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = repository.removeFromCart(itemId)
                if (response.isSuccessful) {
                    _cart.value = response.body()
                    _message.value = "Đã xóa sản phẩm"
                } else {
                    _message.value = buildErrorMessage("Xóa sản phẩm thất bại", response)
                }
            } catch (e: Exception) {
                _message.value = "Lỗi: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    private fun buildErrorMessage(prefix: String, response: Response<*>): String {
        val body = runCatching { response.errorBody()?.string().orEmpty() }.getOrDefault("")
        val apiError = runCatching { gson.fromJson(body, ApiErrorResponse::class.java) }.getOrNull()
        val message = apiError?.message?.takeIf { it.isNotBlank() }
        val path = apiError?.path?.takeIf { it.isNotBlank() }
        val compactBody = body
            .replace("\n", " ")
            .replace("\r", " ")
            .replace(Regex("\\s+"), " ")
            .trim()

        return when {
            message != null && path != null -> "$prefix: ${response.code()} - $message ($path)"
            message != null -> "$prefix: ${response.code()} - $message"
            compactBody.isNotEmpty() -> "$prefix: ${response.code()} - $compactBody"
            else -> "$prefix: ${response.code()}"
        }
    }
}

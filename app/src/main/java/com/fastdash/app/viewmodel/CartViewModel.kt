package com.fastdash.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fastdash.app.data.model.response.CartResponse
import com.fastdash.app.data.repository.CartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CartViewModel(private val repository: CartRepository) : ViewModel() {
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
                    _message.value = "Không thể tải giỏ hàng: ${response.code()}"
                }
            } catch (e: Exception) {
                _message.value = "Lỗi kết nối: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun addToCart(productId: Long, quantity: Int, size: String?, toppingIds: List<Long>) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = repository.addToCart(productId, quantity, size, toppingIds)
                if (response.isSuccessful) {
                    _cart.value = response.body()
                    _message.value = "Đã thêm vào giỏ hàng"
                } else {
                    _message.value = "Thêm vào giỏ thất bại: ${response.code()}"
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
                    _message.value = "Xóa thất bại: ${response.code()}"
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
}

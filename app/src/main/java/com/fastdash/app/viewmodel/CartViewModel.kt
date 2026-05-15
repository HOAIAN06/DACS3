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
                    _message.value = "Khong the tai gio hang: ${response.code()}"
                }
            } catch (e: Exception) {
                _message.value = "Loi ket noi: ${e.message}"
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
                    _message.value = "Da them vao gio hang"
                } else {
                    _message.value = "Them vao gio that bai: ${response.code()}"
                }
            } catch (e: Exception) {
                _message.value = "Loi: ${e.message}"
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
                    _message.value = "Da cap nhat gio hang"
                } else {
                    _message.value = "Cap nhat that bai: ${response.code()}"
                }
            } catch (e: Exception) {
                _message.value = "Loi: ${e.message}"
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
                    _message.value = "Da xoa san pham"
                } else {
                    _message.value = "Xoa that bai: ${response.code()}"
                }
            } catch (e: Exception) {
                _message.value = "Loi: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}

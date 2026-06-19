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
                    _message.value = buildErrorMessage("Khong the tai gio hang", response)
                }
            } catch (e: Exception) {
                _message.value = "Loi ket noi: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun addToCart(
        productId: Long,
        quantity: Int,
        productSizeId: Long?,
        toppingIds: List<Long>,
        note: String? = null
    ) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = repository.addToCart(productId, quantity, productSizeId, toppingIds, note)
                if (response.isSuccessful) {
                    _cart.value = response.body()
                    _message.value = "Da them vao gio hang"
                } else {
                    _message.value = buildErrorMessage("Them vao gio that bai", response)
                }
            } catch (e: Exception) {
                _message.value = "Loi: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun replaceCartItem(
        oldItemId: Long,
        productId: Long,
        quantity: Int,
        productSizeId: Long?,
        toppingIds: List<Long>,
        note: String? = null
    ) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val addResponse = repository.addToCart(productId, quantity, productSizeId, toppingIds, note)
                if (!addResponse.isSuccessful) {
                    _message.value = buildErrorMessage("Cap nhat tuy chon that bai", addResponse)
                    return@launch
                }

                val removeResponse = repository.removeFromCart(oldItemId)
                if (removeResponse.isSuccessful) {
                    _cart.value = removeResponse.body()
                    _message.value = "Da cap nhat mon trong gio"
                } else {
                    _cart.value = addResponse.body()
                    _message.value = buildErrorMessage("Da them cau hinh moi nhung chua xoa duoc mon cu", removeResponse)
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
                    _message.value = buildErrorMessage("Cap nhat gio hang that bai", response)
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
                    val refreshedCartResponse = repository.getCart()
                    if (refreshedCartResponse.isSuccessful) {
                        val refreshedCart = refreshedCartResponse.body()
                        _cart.value = refreshedCart
                        val stillExists = refreshedCart?.items.orEmpty().any { it.id == itemId }
                        _message.value = if (stillExists) {
                            "Backend tra ve thanh cong nhung san pham van con trong gio"
                        } else {
                            "Da xoa san pham"
                        }
                    } else {
                        _cart.value = response.body()
                        _message.value = "Da gui lenh xoa, nhung khong the tai lai gio hang de xac nhan"
                    }
                } else {
                    _message.value = buildErrorMessage("Xoa san pham that bai", response)
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

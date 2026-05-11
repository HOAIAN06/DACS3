package com.fastdash.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fastdash.app.data.model.request.CreateOrderRequest
import com.fastdash.app.data.model.response.OrderResponse
import com.fastdash.app.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OrderViewModel(private val repository: OrderRepository) : ViewModel() {
    private val _orders = MutableStateFlow<List<OrderResponse>>(emptyList())
    val orders: StateFlow<List<OrderResponse>> = _orders.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    fun loadOrders() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = repository.getOrders()
                if (response.isSuccessful) {
                    _orders.value = response.body().orEmpty()
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _loading.value = false
            }
        }
    }

    suspend fun createOrder(deliveryAddress: String, items: List<com.fastdash.app.data.model.request.OrderItemRequest>): Boolean {
        _loading.value = true
        return try {
            val response = repository.createOrder(CreateOrderRequest(deliveryAddress, items))
            response.isSuccessful
        } catch (e: Exception) {
            false
        } finally {
            _loading.value = false
        }
    }
}

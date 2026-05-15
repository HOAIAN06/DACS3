package com.fastdash.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fastdash.app.data.model.request.CheckoutRequest
import com.fastdash.app.data.model.request.CreateOrderFromCartRequest
import com.fastdash.app.data.model.request.CreateOrderRequest
import com.fastdash.app.data.model.request.OrderItemRequest
import com.fastdash.app.data.model.response.OrderResponse
import com.fastdash.app.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OrderViewModel(private val repository: OrderRepository) : ViewModel() {
    private val _orders = MutableStateFlow<List<OrderResponse>>(emptyList())
    val orders: StateFlow<List<OrderResponse>> = _orders.asStateFlow()

    private val _selectedOrder = MutableStateFlow<OrderResponse?>(null)
    val selectedOrder: StateFlow<OrderResponse?> = _selectedOrder.asStateFlow()

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
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadOrderDetail(orderId: Long) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = repository.getOrderDetail(orderId)
                if (response.isSuccessful) {
                    _selectedOrder.value = response.body()
                }
            } finally {
                _loading.value = false
            }
        }
    }

    fun clearSelectedOrder() {
        _selectedOrder.value = null
    }

    suspend fun createOrder(request: CreateOrderRequest): Boolean {
        _loading.value = true
        return try {
            repository.createOrder(request).isSuccessful
        } finally {
            _loading.value = false
        }
    }

    suspend fun createOrder(deliveryAddress: String, items: List<OrderItemRequest>): Boolean {
        return createOrder(
            CreateOrderRequest(
                branchId = 1L,
                deliveryType = "DELIVERY",
                receiverName = "",
                receiverPhone = "",
                deliveryAddress = deliveryAddress,
                paymentMethod = "COD",
                items = items
            )
        )
    }

    suspend fun createOrderFromCart(request: CheckoutRequest): Boolean {
        _loading.value = true
        return try {
            val response = repository.createOrderFromCart(
                CreateOrderFromCartRequest(
                    branchId = request.branchId,
                    deliveryType = request.deliveryType,
                    receiverName = request.receiverName,
                    receiverPhone = request.receiverPhone,
                    deliveryAddress = request.deliveryAddress,
                    note = request.note,
                    paymentMethod = request.paymentMethod
                )
            )
            if (response.isSuccessful) {
                _selectedOrder.value = response.body()
                true
            } else {
                false
            }
        } finally {
            _loading.value = false
        }
    }
}

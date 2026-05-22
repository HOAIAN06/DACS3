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

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun loadOrders() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = repository.getOrders()
                if (response.isSuccessful) {
                    _orders.value = response.body().orEmpty()
                } else {
                    _message.value = "Khong the tai danh sach don hang"
                }
            } catch (e: Exception) {
                _message.value = "Loi ket noi don hang: ${e.message}"
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
                } else {
                    _message.value = "Khong the tai chi tiet don hang"
                }
            } catch (e: Exception) {
                _message.value = "Loi ket noi chi tiet don hang: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun clearSelectedOrder() {
        _selectedOrder.value = null
    }

    fun clearMessage() {
        _message.value = null
    }

    suspend fun cancelOrder(orderId: Long): Boolean {
        _loading.value = true
        return try {
            val response = repository.cancelOrder(orderId)
            if (response.isSuccessful) {
                _selectedOrder.value = response.body()
                loadOrders()
                true
            } else {
                _message.value = "Không thể hủy đơn hàng"
                false
            }
        } catch (e: Exception) {
            _message.value = "Lỗi hủy đơn hàng: ${e.message}"
            false
        } finally {
            _loading.value = false
        }
    }

    suspend fun createOrder(request: CreateOrderRequest): OrderResponse? {
        _loading.value = true
        return try {
            val response = repository.createOrder(request)
            if (response.isSuccessful) {
                _selectedOrder.value = response.body()
                response.body()
            } else {
                null
            }
        } finally {
            _loading.value = false
        }
    }

    suspend fun createOrder(deliveryAddress: String, items: List<OrderItemRequest>): OrderResponse? {
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

    suspend fun createOrderFromCart(request: CheckoutRequest): OrderResponse? {
        _loading.value = true
        return try {
            val response = repository.createOrderFromCart(
                CreateOrderFromCartRequest(
                    branchId = request.branchId,
                    deliveryType = request.deliveryType,
                    receiverName = request.receiverName,
                    receiverPhone = request.receiverPhone,
                    deliveryAddress = request.deliveryAddress,
                    deliveryLatitude = request.deliveryLatitude,
                    deliveryLongitude = request.deliveryLongitude,
                    note = request.note,
                    paymentMethod = request.paymentMethod
                )
            )
            if (response.isSuccessful) {
                _selectedOrder.value = response.body()
                response.body()
            } else {
                null
            }
        } finally {
            _loading.value = false
        }
    }
}

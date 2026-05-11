package com.fastdash.app.data.repository

import android.content.Context
import com.fastdash.app.data.model.request.CreateOrderRequest
import com.fastdash.app.data.model.response.OrderResponse
import com.fastdash.app.data.remote.retrofit.RetrofitClient
import retrofit2.Response

class OrderRepository(private val context: Context) {

	suspend fun getOrders(): Response<List<OrderResponse>> {
		return RetrofitClient.orderApi(context).getOrders()
	}

	suspend fun getOrderDetail(orderId: Long): Response<OrderResponse> {
		return RetrofitClient.orderApi(context).getOrderDetail(orderId)
	}

	suspend fun createOrder(request: CreateOrderRequest): Response<OrderResponse> {
		return RetrofitClient.orderApi(context).createOrder(request)
	}

	suspend fun cancelOrder(orderId: Long): Response<OrderResponse> {
		return RetrofitClient.orderApi(context).cancelOrder(orderId)
	}
}

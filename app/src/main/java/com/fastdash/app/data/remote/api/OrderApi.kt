package com.fastdash.app.data.remote.api

import com.fastdash.app.data.model.request.CreateOrderRequest
import com.fastdash.app.data.model.response.OrderResponse
import retrofit2.Response
import retrofit2.http.*

interface OrderApi {

	@GET("api/v1/orders/my")
	suspend fun getOrders(): Response<List<OrderResponse>>

	@GET("api/v1/orders/{orderId}")
	suspend fun getOrderDetail(@Path("orderId") orderId: Long): Response<OrderResponse>

	@POST("api/v1/orders")
	suspend fun createOrder(@Body request: CreateOrderRequest): Response<OrderResponse>

	@PUT("api/v1/orders/{id}/cancel")
	suspend fun cancelOrder(@Path("id") id: Long): Response<OrderResponse>
}

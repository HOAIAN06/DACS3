package com.fastdash.app.data.repository

import android.content.Context
import com.fastdash.app.data.model.request.CreateOrderFromCartRequest
import com.fastdash.app.data.model.request.CreateOrderRequest
import com.fastdash.app.data.model.request.ShippingFeeQuoteRequest
import com.fastdash.app.data.model.response.OrderResponse
import com.fastdash.app.data.model.response.ShippingFeeQuoteResponse
import com.fastdash.app.data.remote.retrofit.RetrofitClient
import retrofit2.Response
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import com.fastdash.app.utils.TokenManager

class OrderRepository(private val context: Context) {

	suspend fun getOrders(): Response<List<OrderResponse>> {
			val role = TokenManager(context).getRole()?.uppercase()
			return if (role == "ADMIN") {
				val response = RetrofitClient.adminOrderApi(context).getOrders(page = 0, size = 50)
				if (response.isSuccessful) {
					val page = response.body()
					Response.success(
						page?.content.orEmpty().map { summary ->
							OrderResponse(
								id = summary.id,
								orderCode = summary.orderCode,
								status = summary.status,
								createdAt = summary.createdAt,
								deliveryType = summary.deliveryType,
								deliveryAddress = null,
								totalAmount = summary.totalAmount
							)
						}
					)
				} else {
					Response.error(
						response.code(),
						(response.errorBody()?.string().orEmpty()).toResponseBody("text/plain".toMediaType())
					)
				}
			} else {
				RetrofitClient.orderApi(context).getOrders()
			}
	}

	suspend fun getOrderDetail(orderId: Long): Response<OrderResponse> {
			val role = TokenManager(context).getRole()?.uppercase()
			return if (role == "ADMIN") {
				RetrofitClient.adminOrderApi(context).getOrderDetail(orderId)
			} else {
				RetrofitClient.orderApi(context).getOrderDetail(orderId)
			}
	}

	suspend fun createOrder(request: CreateOrderRequest): Response<OrderResponse> {
		return RetrofitClient.orderApi(context).createOrder(request)
	}

	suspend fun createOrderFromCart(request: CreateOrderFromCartRequest): Response<OrderResponse> {
		return RetrofitClient.orderApi(context).createOrderFromCart(request)
	}

	suspend fun cancelOrder(orderId: Long): Response<OrderResponse> {
		return RetrofitClient.orderApi(context).cancelOrder(orderId)
	}

	suspend fun getShippingFeeQuote(branchId: Long, latitude: Double, longitude: Double): Response<ShippingFeeQuoteResponse> {
		return RetrofitClient.orderApi(context).getShippingFeeQuote(
			ShippingFeeQuoteRequest(
				branchId = branchId,
				latitude = latitude,
				longitude = longitude
			)
		)
	}
}

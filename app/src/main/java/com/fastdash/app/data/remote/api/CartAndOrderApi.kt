package com.fastdash.app.data.remote.api

import com.fastdash.app.data.model.request.AddToCartRequest
import com.fastdash.app.data.model.request.CheckoutRequest
import com.fastdash.app.data.model.response.CartResponse
import com.fastdash.app.data.model.response.OrderResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface CartApi {

	@GET("api/v1/cart")
	suspend fun getCart(): Response<CartResponse>

	@POST("api/v1/cart/items")
	suspend fun addToCart(@Body request: AddToCartRequest): Response<CartResponse>

	@PUT("api/v1/cart/items/{itemId}")
	suspend fun updateCartItem(
		@Path("itemId") itemId: Long,
		@Body request: AddToCartRequest
	): Response<CartResponse>

	@DELETE("api/v1/cart/items/{itemId}")
	suspend fun removeFromCart(@Path("itemId") itemId: Long): Response<CartResponse>

	@DELETE("api/v1/cart/clear")
	suspend fun clearCart(): Response<Void>
}


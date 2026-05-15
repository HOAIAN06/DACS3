package com.fastdash.app.data.repository

import android.content.Context
import com.fastdash.app.data.model.request.AddToCartRequest
import com.fastdash.app.data.model.request.UpdateCartItemRequest
import com.fastdash.app.data.model.response.CartResponse
import com.fastdash.app.data.remote.retrofit.RetrofitClient
import retrofit2.Response

class CartRepository(private val context: Context) {

	suspend fun getCart(): Response<CartResponse> {
		return RetrofitClient.cartApi(context).getCart()
	}

	suspend fun addToCart(
		productId: Long,
		quantity: Int,
		productSizeId: Long? = null,
		toppingIds: List<Long> = emptyList(),
		note: String? = null
	): Response<CartResponse> {
		return RetrofitClient.cartApi(context).addToCart(
			AddToCartRequest(
				productId = productId,
				productSizeId = productSizeId,
				toppingIds = toppingIds,
				quantity = quantity,
				note = note
			)
		)
	}

	suspend fun updateCartItem(itemId: Long, quantity: Int, note: String? = null): Response<CartResponse> {
		return RetrofitClient.cartApi(context).updateCartItem(
			itemId,
			UpdateCartItemRequest(quantity = quantity, note = note)
		)
	}

	suspend fun removeFromCart(itemId: Long): Response<CartResponse> {
		return RetrofitClient.cartApi(context).removeFromCart(itemId)
	}

	suspend fun clearCart(): Response<Void> {
		return RetrofitClient.cartApi(context).clearCart()
	}
}


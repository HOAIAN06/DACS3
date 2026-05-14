package com.fastdash.app.data.model.response

import com.google.gson.annotations.SerializedName

data class ProductSizeResponse(
	val id: Long,
	val productId: Long,
	@SerializedName(value = "sizeName", alternate = ["size_name", "name"]) val sizeName: String?,
	val price: Double
)


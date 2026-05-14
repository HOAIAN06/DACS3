package com.fastdash.app.data.model.response

import com.google.gson.annotations.SerializedName

data class ToppingResponse(
	val id: Long,
	@SerializedName(value = "name", alternate = ["toppingName", "topping_name"]) val name: String?,
	val price: Double,
	@SerializedName(value = "imageUrl", alternate = ["image_url", "url", "secure_url"]) val imageUrl: String? = null
)



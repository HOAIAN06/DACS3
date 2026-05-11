package com.fastdash.app.data.model.response

data class ToppingResponse(
	val id: Long,
	val name: String,
	val price: Double,
	val imageUrl: String? = null
)



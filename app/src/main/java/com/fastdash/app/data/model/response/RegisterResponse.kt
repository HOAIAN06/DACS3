package com.fastdash.app.data.model.response

data class RegisterResponse(
	val id: Long,
	val fullName: String,
	val email: String,
	val phone: String,
	val message: String
)


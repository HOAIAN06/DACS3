package com.fastdash.app.data.model.request

data class RegisterRequest(
	val fullName: String,
	val email: String,
	val phone: String,
	val password: String
)

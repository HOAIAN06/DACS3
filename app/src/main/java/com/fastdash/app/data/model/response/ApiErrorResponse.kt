package com.fastdash.app.data.model.response

data class ApiErrorResponse(
    val message: String? = null,
    val error: String? = null,
    val details: String? = null,
    val path: String? = null
) {
    fun displayMessage(): String? = message ?: error ?: details
}

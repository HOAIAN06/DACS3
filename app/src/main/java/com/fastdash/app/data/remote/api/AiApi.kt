package com.fastdash.app.data.remote.api

import com.fastdash.app.data.model.request.AiChatRequest
import com.fastdash.app.data.model.response.AiChatResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface AiApi {
    @POST("/api/v1/ai/chat")
    suspend fun chatWithAi(
        @Header("Authorization") token: String,
        @Body request: AiChatRequest
    ): AiChatResponse
}

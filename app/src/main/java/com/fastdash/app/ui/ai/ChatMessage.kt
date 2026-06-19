package com.fastdash.app.ui.ai

import com.fastdash.app.data.model.response.AiSuggestedProduct

data class ChatMessage(
    val id: Long,
    val content: String,
    val isUser: Boolean,
    val suggestedProducts: List<AiSuggestedProduct> = emptyList(),
    val intent: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

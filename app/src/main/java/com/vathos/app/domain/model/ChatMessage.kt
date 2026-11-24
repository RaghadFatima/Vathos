
package com.vathos.app.domain.model

data class ChatMessage(
    val id: Long,
    val conversationId: String,
    val documentId: Long? = null,
    val message: String,
    val isUser: Boolean,
    val timestamp: Long
)

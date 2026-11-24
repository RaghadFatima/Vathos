
package com.vathos.app.domain.usecase

import com.vathos.app.domain.model.ChatMessage
import com.vathos.app.domain.repository.ChatRepository

class SendMessageUseCase(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(message: ChatMessage): Long {
        require(message.conversationId.isNotBlank()) {
            "Conversation ID cannot be empty"
        }

        require(message.message.isNotBlank()) {
            "Message cannot be empty"
        }

        return repository.sendMessage(message)
    }
}

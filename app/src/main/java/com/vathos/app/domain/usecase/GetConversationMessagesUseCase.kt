
package com.vathos.app.domain.usecase

import com.vathos.app.domain.model.ChatMessage
import com.vathos.app.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow

class GetConversationMessagesUseCase(
    private val repository: ChatRepository
) {
    operator fun invoke(
        conversationId: String
    ): Flow<List<ChatMessage>> {
        return repository.getConversationMessages(conversationId)
    }
}

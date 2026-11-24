
package com.vathos.app.domain.usecase

import com.vathos.app.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow

class GetConversationCountUseCase(
    private val chatRepository: ChatRepository
) {
    operator fun invoke(): Flow<Int> {
        return chatRepository.getConversationCount()
    }
}

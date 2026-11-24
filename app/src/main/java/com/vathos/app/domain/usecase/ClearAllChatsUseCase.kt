
package com.vathos.app.domain.usecase

import com.vathos.app.domain.repository.ChatRepository

class ClearAllChatsUseCase(
    private val repository: ChatRepository
) {
    suspend operator fun invoke() {
        repository.deleteAllMessages()
    }
}

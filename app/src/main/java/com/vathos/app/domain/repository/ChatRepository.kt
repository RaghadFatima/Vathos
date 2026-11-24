
package com.vathos.app.domain.repository

import com.vathos.app.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {

    fun getConversationMessages(
        conversationId: String
    ): Flow<List<ChatMessage>>

    fun getAllMessages(): Flow<List<ChatMessage>>

    fun getMessagesForDocument(
        documentId: Long
    ): Flow<List<ChatMessage>>

    fun getConversationCount(): Flow<Int>

    suspend fun sendMessage(message: ChatMessage): Long

    suspend fun deleteConversation(conversationId: String)

    suspend fun deleteAllMessages()
}

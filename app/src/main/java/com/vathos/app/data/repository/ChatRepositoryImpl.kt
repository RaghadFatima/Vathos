
package com.vathos.app.data.repository

import com.vathos.app.data.local.dao.ChatDao
import com.vathos.app.data.local.entity.ChatMessageEntity
import com.vathos.app.domain.model.ChatMessage
import com.vathos.app.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ChatRepositoryImpl(
    private val chatDao: ChatDao
) : ChatRepository {

    override fun getConversationMessages(
        conversationId: String
    ): Flow<List<ChatMessage>> {
        return chatDao.getConversationMessages(conversationId)
            .map { messages ->
                messages.map { it.toDomain() }
            }
    }

    override fun getAllMessages(): Flow<List<ChatMessage>> {
        return chatDao.getAllMessages().map { messages ->
            messages.map { it.toDomain() }
        }
    }

    override fun getMessagesForDocument(
        documentId: Long
    ): Flow<List<ChatMessage>> {
        return chatDao.getMessagesForDocument(documentId)
            .map { messages ->
                messages.map { it.toDomain() }
            }
    }

    override fun getConversationCount(): Flow<Int> {
        return chatDao.getConversationCount()
    }

    override suspend fun sendMessage(message: ChatMessage): Long {
        return chatDao.insertMessage(
            ChatMessageEntity(
                id = message.id,
                conversationId = message.conversationId,
                documentId = message.documentId,
                message = message.message,
                isUser = message.isUser,
                timestamp = message.timestamp
            )
        )
    }

    override suspend fun deleteConversation(conversationId: String) {
        chatDao.deleteConversation(conversationId)
    }

    override suspend fun deleteAllMessages() {
        chatDao.deleteAllMessages()
    }

    private fun ChatMessageEntity.toDomain(): ChatMessage {
        return ChatMessage(
            id = id,
            conversationId = conversationId,
            documentId = documentId,
            message = message,
            isUser = isUser,
            timestamp = timestamp
        )
    }
}


package com.vathos.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vathos.app.data.local.entity.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Query(
        "SELECT * FROM chat_messages " +
                "WHERE conversationId = :conversationId " +
                "ORDER BY timestamp ASC"
    )
    fun getConversationMessages(
        conversationId: String
    ): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessageEntity>>

    @Query(
        "SELECT * FROM chat_messages " +
                "WHERE documentId = :documentId " +
                "ORDER BY timestamp ASC"
    )
    fun getMessagesForDocument(
        documentId: Long
    ): Flow<List<ChatMessageEntity>>

    @Query("SELECT COUNT(DISTINCT conversationId) FROM chat_messages")
    fun getConversationCount(): Flow<Int>

    @Query("DELETE FROM chat_messages")
    suspend fun deleteAllMessages()

    @Query("DELETE FROM chat_messages WHERE conversationId = :conversationId")
    suspend fun deleteConversation(conversationId: String)
}

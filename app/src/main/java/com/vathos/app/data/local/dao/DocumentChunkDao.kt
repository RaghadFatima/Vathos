
package com.vathos.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vathos.app.data.local.entity.DocumentChunkEntity

@Dao
interface DocumentChunkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChunks(
        chunks: List<DocumentChunkEntity>
    ): List<Long>

    @Query("""
        SELECT * FROM document_chunks
        WHERE documentId = :documentId
        ORDER BY chunkIndex ASC
    """)
    suspend fun getChunksForDocument(
        documentId: Long
    ): List<DocumentChunkEntity>

    @Query("""
        UPDATE document_chunks
        SET embedding = :embedding
        WHERE id = :chunkId
    """)
    suspend fun updateChunkEmbedding(
        chunkId: Long,
        embedding: String
    )

    @Query("DELETE FROM document_chunks WHERE documentId = :documentId")
    suspend fun deleteChunksForDocument(documentId: Long)

    @Query("DELETE FROM document_chunks")
    suspend fun deleteAllChunks()

    @Query("""
        SELECT COUNT(*) FROM document_chunks
        WHERE documentId = :documentId
    """)
    suspend fun getChunkCount(documentId: Long): Int
}

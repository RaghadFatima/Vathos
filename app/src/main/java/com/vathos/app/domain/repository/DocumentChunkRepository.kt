
package com.vathos.app.domain.repository

import com.vathos.app.data.local.entity.DocumentChunkEntity

interface DocumentChunkRepository {

    suspend fun saveChunks(
        documentId: Long,
        chunks: List<String>
    )

    suspend fun getChunks(
        documentId: Long
    ): List<DocumentChunkEntity>

    suspend fun updateEmbedding(
        chunkId: Long,
        embedding: String
    )

    suspend fun deleteChunks(
        documentId: Long
    )

    suspend fun getChunkCount(
        documentId: Long
    ): Int
}

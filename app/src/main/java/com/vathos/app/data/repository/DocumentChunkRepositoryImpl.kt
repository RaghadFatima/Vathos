
package com.vathos.app.data.repository

import com.vathos.app.data.local.dao.DocumentChunkDao
import com.vathos.app.data.local.entity.DocumentChunkEntity
import com.vathos.app.domain.repository.DocumentChunkRepository

class DocumentChunkRepositoryImpl(
    private val documentChunkDao: DocumentChunkDao
) : DocumentChunkRepository {

    override suspend fun saveChunks(
        documentId: Long,
        chunks: List<String>
    ) {
        require(documentId > 0) {
            "Invalid document ID"
        }

        documentChunkDao.deleteChunksForDocument(documentId)

        if (chunks.isEmpty()) return

        val entities = chunks.mapIndexed { index, content ->
            DocumentChunkEntity(
                documentId = documentId,
                chunkIndex = index,
                content = content
            )
        }

        documentChunkDao.insertChunks(entities)
    }

    override suspend fun getChunks(
        documentId: Long
    ): List<DocumentChunkEntity> {
        return documentChunkDao.getChunksForDocument(documentId)
    }

    override suspend fun updateEmbedding(
        chunkId: Long,
        embedding: String
    ) {
        documentChunkDao.updateChunkEmbedding(
            chunkId,
            embedding
        )
    }

    override suspend fun deleteChunks(documentId: Long) {
        documentChunkDao.deleteChunksForDocument(documentId)
    }

    override suspend fun getChunkCount(documentId: Long): Int {
        return documentChunkDao.getChunkCount(documentId)
    }
}

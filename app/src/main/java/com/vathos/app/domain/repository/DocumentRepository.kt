
package com.vathos.app.domain.repository

import com.vathos.app.domain.model.Document
import kotlinx.coroutines.flow.Flow

interface DocumentRepository {

    fun getAllDocuments(): Flow<List<Document>>

    fun searchDocuments(query: String): Flow<List<Document>>

    suspend fun getDocumentById(id: Long): Document?

    suspend fun addDocument(document: Document): Long

    suspend fun updateExtractedText(id: Long, text: String)

    suspend fun updateSummary(id: Long, summary: String)

    suspend fun deleteDocument(id: Long)

    suspend fun deleteAllDocuments()
}

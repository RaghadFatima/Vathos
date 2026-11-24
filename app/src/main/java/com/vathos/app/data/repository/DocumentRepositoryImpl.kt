
package com.vathos.app.data.repository

import com.vathos.app.data.local.dao.DocumentDao
import com.vathos.app.data.local.entity.DocumentEntity
import com.vathos.app.domain.model.Document
import com.vathos.app.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DocumentRepositoryImpl(
    private val documentDao: DocumentDao
) : DocumentRepository {

    override fun getAllDocuments(): Flow<List<Document>> {
        return documentDao.getAllDocuments().map { documents ->
            documents.map { it.toDomain() }
        }
    }

    override fun searchDocuments(query: String): Flow<List<Document>> {
        return if (query.isBlank()) {
            getAllDocuments()
        } else {
            documentDao.searchDocuments(query.trim()).map { documents ->
                documents.map { it.toDomain() }
            }
        }
    }

    override suspend fun getDocumentById(id: Long): Document? {
        return documentDao.getDocumentById(id)?.toDomain()
    }

    override suspend fun addDocument(document: Document): Long {
        return documentDao.insertDocument(
            DocumentEntity(
                id = document.id,
                name = document.name,
                size = document.size,
                date = document.date,
                pages = document.pages,
                filePath = document.filePath,
                summary = document.summary,
                extractedText = document.extractedText
            )
        )
    }

    override suspend fun updateExtractedText(id: Long, text: String) {
        documentDao.updateExtractedText(id, text)
    }

    override suspend fun updateSummary(id: Long, summary: String) {
        documentDao.updateSummary(id, summary)
    }

    override suspend fun deleteDocument(id: Long) {
        documentDao.deleteDocumentById(id)
    }

    override suspend fun deleteAllDocuments() {
        documentDao.deleteAllDocuments()
    }

    private fun DocumentEntity.toDomain(): Document {
        return Document(
            id = id,
            name = name,
            size = size,
            date = date,
            pages = pages,
            filePath = filePath,
            summary = summary,
            extractedText = extractedText
        )
    }
}

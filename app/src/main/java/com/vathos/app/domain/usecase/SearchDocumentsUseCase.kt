
package com.vathos.app.domain.usecase

import com.vathos.app.domain.model.Document
import com.vathos.app.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow

class SearchDocumentsUseCase(
    private val repository: DocumentRepository
) {
    operator fun invoke(query: String): Flow<List<Document>> {
        return repository.searchDocuments(query)
    }
}

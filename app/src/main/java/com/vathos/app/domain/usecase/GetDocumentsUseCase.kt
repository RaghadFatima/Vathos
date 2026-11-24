
package com.vathos.app.domain.usecase

import com.vathos.app.domain.model.Document
import com.vathos.app.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow

class GetDocumentsUseCase(
    private val repository: DocumentRepository
) {
    operator fun invoke(): Flow<List<Document>> {
        return repository.getAllDocuments()
    }
}

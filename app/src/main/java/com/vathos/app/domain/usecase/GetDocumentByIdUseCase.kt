
package com.vathos.app.domain.usecase

import com.vathos.app.domain.model.Document
import com.vathos.app.domain.repository.DocumentRepository

class GetDocumentByIdUseCase(
    private val repository: DocumentRepository
) {
    suspend operator fun invoke(documentId: Long): Document? {
        return repository.getDocumentById(documentId)
    }
}

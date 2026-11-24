
package com.vathos.app.domain.usecase

import com.vathos.app.domain.model.Document
import com.vathos.app.domain.repository.DocumentRepository

class AddDocumentUseCase(
    private val repository: DocumentRepository
) {
    suspend operator fun invoke(document: Document): Long {
        require(document.name.isNotBlank()) {
            "Document name cannot be empty"
        }

        require(document.filePath.isNotBlank()) {
            "Document file path cannot be empty"
        }

        require(document.size >= 0) {
            "Document size cannot be negative"
        }

        return repository.addDocument(document)
    }
}

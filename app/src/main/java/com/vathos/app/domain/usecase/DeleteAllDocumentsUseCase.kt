
package com.vathos.app.domain.usecase

import com.vathos.app.domain.repository.DocumentRepository
import com.vathos.app.domain.repository.PdfFileRepository
import kotlinx.coroutines.flow.first

class DeleteAllDocumentsUseCase(
    private val documentRepository: DocumentRepository,
    private val pdfFileRepository: PdfFileRepository
) {
    suspend operator fun invoke() {
        val documents = documentRepository.getAllDocuments().first()

        // Delete each PDF through the validated private-storage repository.
        documents.forEach { document ->
            pdfFileRepository.deletePdf(document.filePath)
        }

        // Clear database records after all file deletions succeed.
        documentRepository.deleteAllDocuments()
    }
}

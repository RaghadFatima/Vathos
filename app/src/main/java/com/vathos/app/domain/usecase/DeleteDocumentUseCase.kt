
package com.vathos.app.domain.usecase

import com.vathos.app.domain.repository.DocumentRepository
import com.vathos.app.domain.repository.PdfFileRepository

class DeleteDocumentUseCase(
    private val documentRepository: DocumentRepository,
    private val pdfFileRepository: PdfFileRepository
) {
    suspend operator fun invoke(documentId: Long) {
        val document = documentRepository.getDocumentById(documentId)
            ?: return

        // Delete the PDF through the validated private-storage repository.
        pdfFileRepository.deletePdf(document.filePath)

        // Delete the database record only after file deletion succeeds.
        documentRepository.deleteDocument(documentId)
    }
}

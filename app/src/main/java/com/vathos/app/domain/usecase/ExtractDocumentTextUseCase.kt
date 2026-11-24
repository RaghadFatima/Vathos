
package com.vathos.app.domain.usecase

import com.vathos.app.domain.repository.DocumentRepository
import com.vathos.app.domain.repository.PdfTextRepository

class ExtractDocumentTextUseCase(
    private val documentRepository: DocumentRepository,
    private val pdfTextRepository: PdfTextRepository
) {

    suspend operator fun invoke(documentId: Long): String {

        require(documentId > 0) {
            "Invalid document ID"
        }

        val document = documentRepository.getDocumentById(documentId)
            ?: throw IllegalArgumentException("Document not found")

        if (document.extractedText.isNotBlank()) {
            return document.extractedText
        }

        val extractedText = pdfTextRepository.extractText(document.filePath)

        documentRepository.updateExtractedText(
            id = documentId,
            text = extractedText
        )

        return extractedText
    }
}


package com.vathos.app.domain.usecase

import com.vathos.app.data.repository.PdfSummaryGenerator
import com.vathos.app.domain.repository.DocumentRepository
import com.vathos.app.domain.repository.PdfTextRepository

class GenerateDocumentSummaryUseCase(
    private val documentRepository: DocumentRepository,
    private val pdfTextRepository: PdfTextRepository,
    private val summaryGenerator: PdfSummaryGenerator
) {

    suspend operator fun invoke(documentId: Long): String {

        require(documentId > 0) {
            "Invalid document ID"
        }

        val document = documentRepository.getDocumentById(documentId)
            ?: throw IllegalArgumentException("Document not found")

        val extractedText =
            if (document.extractedText.isNotBlank()) {
                document.extractedText
            } else {
                pdfTextRepository.extractText(document.filePath)
                    .also { text ->
                        documentRepository.updateExtractedText(
                            documentId,
                            text
                        )
                    }
            }

        if (extractedText.isBlank()) {
            return "No readable text was found in this PDF."
        }

        val summary = summaryGenerator.generateSummary(extractedText)

        documentRepository.updateSummary(documentId, summary)

        return summary
    }
}

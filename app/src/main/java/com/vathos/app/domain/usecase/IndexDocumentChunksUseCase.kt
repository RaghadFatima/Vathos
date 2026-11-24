
package com.vathos.app.domain.usecase

import com.vathos.app.data.repository.DocumentChunker
import com.vathos.app.domain.repository.DocumentChunkRepository

class IndexDocumentChunksUseCase(
    private val extractDocumentTextUseCase: ExtractDocumentTextUseCase,
    private val documentChunkRepository: DocumentChunkRepository,
    private val documentChunker: DocumentChunker
) {
    suspend operator fun invoke(documentId: Long): Int {
        require(documentId > 0) {
            "Invalid document ID"
        }

        // Reuse existing chunks if this document has already been indexed.
        val existingCount =
            documentChunkRepository.getChunkCount(documentId)

        if (existingCount > 0) {
            return existingCount
        }

        // Extract and cache the PDF text.
        val extractedText =
            extractDocumentTextUseCase(documentId)

        if (extractedText.isBlank()) {
            return 0
        }

        // Split the extracted text into overlapping chunks.
        val chunks =
            documentChunker.splitIntoChunks(extractedText)

        // Save the chunks in the Room database.
        documentChunkRepository.saveChunks(
            documentId = documentId,
            chunks = chunks
        )

        return chunks.size
    }
}

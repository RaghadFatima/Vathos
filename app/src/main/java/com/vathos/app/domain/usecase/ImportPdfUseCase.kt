
package com.vathos.app.domain.usecase

import com.vathos.app.domain.model.Document
import com.vathos.app.domain.repository.DocumentRepository
import com.vathos.app.domain.repository.PdfFileRepository

class ImportPdfUseCase(
    private val pdfFileRepository: PdfFileRepository,
    private val documentRepository: DocumentRepository
) {
    suspend operator fun invoke(uriString: String): Long {

        val importedPdf = pdfFileRepository.importPdf(uriString)

        try {
            val document = Document(
                id = 0,
                name = importedPdf.name,
                size = importedPdf.size,
                date = System.currentTimeMillis(),
                pages = importedPdf.pages,
                filePath = importedPdf.filePath,
                summary = ""
            )

            return documentRepository.addDocument(document)

        } catch (exception: Exception) {
            pdfFileRepository.deletePdf(importedPdf.filePath)
            throw exception
        }
    }
}


package com.vathos.app.data.repository

import com.vathos.app.domain.repository.PdfTextRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PdfTextRepositoryImpl(
    private val pdfTextExtractor: PdfTextExtractor
) : PdfTextRepository {

    override suspend fun extractText(filePath: String): String {
        return withContext(Dispatchers.IO) {
            pdfTextExtractor.extractText(filePath)
        }
    }
}

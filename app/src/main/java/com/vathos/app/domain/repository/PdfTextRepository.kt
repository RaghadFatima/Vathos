
package com.vathos.app.domain.repository

interface PdfTextRepository {
    suspend fun extractText(filePath: String): String
}

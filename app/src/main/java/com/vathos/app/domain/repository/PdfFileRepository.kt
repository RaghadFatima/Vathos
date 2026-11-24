
package com.vathos.app.domain.repository

import com.vathos.app.domain.model.ImportedPdf

interface PdfFileRepository {

    suspend fun importPdf(uriString: String): ImportedPdf

    suspend fun deletePdf(filePath: String)

    suspend fun deleteAllPdfs()
}

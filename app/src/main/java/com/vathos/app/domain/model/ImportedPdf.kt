
package com.vathos.app.domain.model

data class ImportedPdf(
    val name: String,
    val size: Long,
    val pages: Int,
    val filePath: String
)

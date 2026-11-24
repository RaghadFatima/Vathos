
package com.vathos.app.domain.model

data class Document(
    val id: Long,
    val name: String,
    val size: Long,
    val date: Long,
    val pages: Int,
    val filePath: String,
    val summary: String,
    val extractedText: String = ""
)


package com.vathos.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val size: Long,
    val date: Long,
    val pages: Int,
    val filePath: String,
    val summary: String = "",
    val extractedText: String = ""
)

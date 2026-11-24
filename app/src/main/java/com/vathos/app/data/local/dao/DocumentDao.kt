
package com.vathos.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vathos.app.data.local.entity.DocumentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: DocumentEntity): Long

    @Query("SELECT * FROM documents ORDER BY date DESC")
    fun getAllDocuments(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE id = :documentId LIMIT 1")
    suspend fun getDocumentById(documentId: Long): DocumentEntity?

    @Query("""
        SELECT * FROM documents
        WHERE LOWER(name) LIKE '%' || LOWER(:query) || '%'
        OR LOWER(extractedText) LIKE '%' || LOWER(:query) || '%'
        ORDER BY date DESC
    """)
    fun searchDocuments(query: String): Flow<List<DocumentEntity>>

    @Query("UPDATE documents SET extractedText = :text WHERE id = :documentId")
    suspend fun updateExtractedText(documentId: Long, text: String)

    @Query("UPDATE documents SET summary = :summary WHERE id = :documentId")
    suspend fun updateSummary(documentId: Long, summary: String)

    @Query("DELETE FROM documents WHERE id = :documentId")
    suspend fun deleteDocumentById(documentId: Long)

    @Query("DELETE FROM documents")
    suspend fun deleteAllDocuments()

    @Query("SELECT COUNT(*) FROM documents")
    fun getDocumentCount(): Flow<Int>
}

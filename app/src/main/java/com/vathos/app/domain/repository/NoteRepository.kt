
package com.vathos.app.domain.repository

import com.vathos.app.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {

    fun getAllNotes(): Flow<List<Note>>

    suspend fun getNoteById(noteId: Long): Note?

    suspend fun createNote(note: Note): Long

    suspend fun deleteNote(noteId: Long)

    suspend fun deleteAllNotes()
}

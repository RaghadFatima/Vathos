
package com.vathos.app.data.repository

import com.vathos.app.data.local.dao.NoteDao
import com.vathos.app.data.local.entity.NoteEntity
import com.vathos.app.domain.model.Note
import com.vathos.app.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NoteRepositoryImpl(
    private val noteDao: NoteDao
) : NoteRepository {

    override fun getAllNotes(): Flow<List<Note>> {
        return noteDao.getAllNotes().map { notes ->
            notes.map { it.toDomain() }
        }
    }

    override suspend fun getNoteById(noteId: Long): Note? {
        return noteDao.getNoteById(noteId)?.toDomain()
    }

    override suspend fun createNote(note: Note): Long {
        return noteDao.insertNote(
            NoteEntity(
                id = note.id,
                title = note.title,
                content = note.content,
                createdAt = note.createdAt
            )
        )
    }

    override suspend fun deleteNote(noteId: Long) {
        noteDao.deleteNote(noteId)
    }

    override suspend fun deleteAllNotes() {
        noteDao.deleteAllNotes()
    }

    private fun NoteEntity.toDomain(): Note {
        return Note(
            id = id,
            title = title,
            content = content,
            createdAt = createdAt
        )
    }
}

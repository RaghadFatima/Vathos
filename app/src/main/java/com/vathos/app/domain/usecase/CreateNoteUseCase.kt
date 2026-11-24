
package com.vathos.app.domain.usecase

import com.vathos.app.domain.model.Note
import com.vathos.app.domain.repository.NoteRepository

class CreateNoteUseCase(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(
        title: String,
        content: String
    ): Long {
        require(title.isNotBlank()) {
            "Note title cannot be empty"
        }

        require(content.isNotBlank()) {
            "Note content cannot be empty"
        }

        val note = Note(
            id = 0,
            title = title.trim(),
            content = content.trim(),
            createdAt = System.currentTimeMillis()
        )

        return repository.createNote(note)
    }
}

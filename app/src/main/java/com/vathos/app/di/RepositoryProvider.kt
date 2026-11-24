
package com.vathos.app.di

import android.content.Context
import com.vathos.app.data.local.VathosDatabase
import com.vathos.app.data.repository.ChatRepositoryImpl
import com.vathos.app.data.repository.DocumentChunkRepositoryImpl
import com.vathos.app.data.repository.DocumentRepositoryImpl
import com.vathos.app.data.repository.NoteRepositoryImpl
import com.vathos.app.data.repository.PdfFileRepositoryImpl
import com.vathos.app.data.repository.PdfTextExtractor
import com.vathos.app.data.repository.PdfTextRepositoryImpl
import com.vathos.app.data.repository.SettingsRepositoryImpl
import com.vathos.app.data.repository.TaskRepositoryImpl
import com.vathos.app.domain.repository.ChatRepository
import com.vathos.app.domain.repository.DocumentChunkRepository
import com.vathos.app.domain.repository.DocumentRepository
import com.vathos.app.domain.repository.NoteRepository
import com.vathos.app.domain.repository.PdfFileRepository
import com.vathos.app.domain.repository.PdfTextRepository
import com.vathos.app.domain.repository.SettingsRepository
import com.vathos.app.domain.repository.TaskRepository

object RepositoryProvider {

    fun provideDocumentRepository(
        database: VathosDatabase
    ): DocumentRepository {
        return DocumentRepositoryImpl(database.documentDao())
    }

    fun provideDocumentChunkRepository(
        database: VathosDatabase
    ): DocumentChunkRepository {
        return DocumentChunkRepositoryImpl(
            database.documentChunkDao()
        )
    }

    fun provideChatRepository(
        database: VathosDatabase
    ): ChatRepository {
        return ChatRepositoryImpl(database.chatDao())
    }

    fun provideNoteRepository(
        database: VathosDatabase
    ): NoteRepository {
        return NoteRepositoryImpl(database.noteDao())
    }

    fun provideTaskRepository(
        database: VathosDatabase
    ): TaskRepository {
        return TaskRepositoryImpl(database.taskDao())
    }

    fun provideSettingsRepository(
        context: Context
    ): SettingsRepository {
        return SettingsRepositoryImpl(context.applicationContext)
    }

    fun providePdfFileRepository(
        context: Context
    ): PdfFileRepository {
        return PdfFileRepositoryImpl(context.applicationContext)
    }

    fun providePdfTextRepository(
        context: Context
    ): PdfTextRepository {
        val extractor = PdfTextExtractor(context.applicationContext)
        return PdfTextRepositoryImpl(extractor)
    }
}

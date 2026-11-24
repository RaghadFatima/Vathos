
package com.vathos.app.di

import com.vathos.app.data.repository.DocumentChunker
import com.vathos.app.data.repository.PdfSummaryGenerator
import com.vathos.app.domain.repository.ChatRepository
import com.vathos.app.domain.repository.DocumentChunkRepository
import com.vathos.app.domain.repository.DocumentRepository
import com.vathos.app.domain.repository.LocalModelManager
import com.vathos.app.domain.repository.NoteRepository
import com.vathos.app.domain.repository.PdfFileRepository
import com.vathos.app.domain.repository.PdfTextRepository
import com.vathos.app.domain.repository.SettingsRepository
import com.vathos.app.domain.repository.TaskRepository
import com.vathos.app.domain.usecase.AddDocumentUseCase
import com.vathos.app.domain.usecase.ClearAllChatsUseCase
import com.vathos.app.domain.usecase.CreateNoteUseCase
import com.vathos.app.domain.usecase.CreateTaskUseCase
import com.vathos.app.domain.usecase.DeleteAllDocumentsUseCase
import com.vathos.app.domain.usecase.DeleteAllTasksUseCase
import com.vathos.app.domain.usecase.DeleteDocumentUseCase
import com.vathos.app.domain.usecase.DeleteTaskUseCase
import com.vathos.app.domain.usecase.ExportVathosDataUseCase
import com.vathos.app.domain.usecase.ExtractDocumentTextUseCase
import com.vathos.app.domain.usecase.GenerateDocumentSummaryUseCase
import com.vathos.app.domain.usecase.GetAllNotesUseCase
import com.vathos.app.domain.usecase.GetAllTasksUseCase
import com.vathos.app.domain.usecase.GetConversationCountUseCase
import com.vathos.app.domain.usecase.GetConversationMessagesUseCase
import com.vathos.app.domain.usecase.GetDocumentByIdUseCase
import com.vathos.app.domain.usecase.GetDocumentsUseCase
import com.vathos.app.domain.usecase.GetPendingTasksUseCase
import com.vathos.app.domain.usecase.ImportPdfUseCase
import com.vathos.app.domain.usecase.IndexDocumentChunksUseCase
import com.vathos.app.domain.usecase.RetrieveRelevantChunksUseCase
import com.vathos.app.domain.usecase.SearchDocumentsUseCase
import com.vathos.app.domain.usecase.SendMessageUseCase
import com.vathos.app.domain.usecase.UpdateTaskStatusUseCase

object UseCaseProvider {

    fun provideGetDocuments(repository: DocumentRepository) =
        GetDocumentsUseCase(repository)

    fun provideSearchDocuments(repository: DocumentRepository) =
        SearchDocumentsUseCase(repository)

    fun provideAddDocument(repository: DocumentRepository) =
        AddDocumentUseCase(repository)

    fun provideDeleteDocument(
        documentRepository: DocumentRepository,
        pdfFileRepository: PdfFileRepository
    ) = DeleteDocumentUseCase(documentRepository, pdfFileRepository)

    fun provideDeleteAllDocuments(
        documentRepository: DocumentRepository,
        pdfFileRepository: PdfFileRepository
    ) = DeleteAllDocumentsUseCase(documentRepository, pdfFileRepository)

    fun provideGetDocumentById(repository: DocumentRepository) =
        GetDocumentByIdUseCase(repository)

    fun provideImportPdf(
        pdfRepository: PdfFileRepository,
        documentRepository: DocumentRepository
    ) = ImportPdfUseCase(pdfRepository, documentRepository)

    fun provideExtractDocumentText(
        documentRepository: DocumentRepository,
        pdfTextRepository: PdfTextRepository
    ) = ExtractDocumentTextUseCase(
        documentRepository,
        pdfTextRepository
    )

    fun provideGenerateDocumentSummary(
        documentRepository: DocumentRepository,
        pdfTextRepository: PdfTextRepository,
        localModelManager: LocalModelManager
    ) = GenerateDocumentSummaryUseCase(
        documentRepository,
        pdfTextRepository,
        PdfSummaryGenerator(localModelManager)
    )

    fun provideRetrieveRelevantChunks(
        documentChunkRepository: DocumentChunkRepository,
        embeddingGemmaEmbedder: com.vathos.app.data.ai.EmbeddingGemmaEmbedder
    ) = RetrieveRelevantChunksUseCase(
        documentChunkRepository,
        embeddingGemmaEmbedder
    )

    fun provideIndexDocumentChunks(
        extractDocumentTextUseCase: ExtractDocumentTextUseCase,
        documentChunkRepository: DocumentChunkRepository,
        documentChunker: DocumentChunker
    ) = IndexDocumentChunksUseCase(
        extractDocumentTextUseCase,
        documentChunkRepository,
        documentChunker
    )

    fun provideGetConversationMessages(repository: ChatRepository) =
        GetConversationMessagesUseCase(repository)

    fun provideGetConversationCount(repository: ChatRepository) =
        GetConversationCountUseCase(repository)

    fun provideSendMessage(repository: ChatRepository) =
        SendMessageUseCase(repository)

    fun provideClearAllChats(repository: ChatRepository) =
        ClearAllChatsUseCase(repository)

    fun provideCreateNote(repository: NoteRepository) =
        CreateNoteUseCase(repository)

    fun provideGetAllNotes(repository: NoteRepository) =
        GetAllNotesUseCase(repository)

    fun provideCreateTask(repository: TaskRepository) =
        CreateTaskUseCase(repository)

    fun provideGetAllTasks(repository: TaskRepository) =
        GetAllTasksUseCase(repository)

    fun provideGetPendingTasks(repository: TaskRepository) =
        GetPendingTasksUseCase(repository)

    fun provideUpdateTaskStatus(repository: TaskRepository) =
        UpdateTaskStatusUseCase(repository)

    fun provideDeleteTask(repository: TaskRepository) =
        DeleteTaskUseCase(repository)

    fun provideDeleteAllTasks(repository: TaskRepository) =
        DeleteAllTasksUseCase(repository)

    fun provideSettingsRepository(repository: SettingsRepository) =
        repository

    fun provideExportVathosData(
        documentRepository: DocumentRepository,
        chatRepository: ChatRepository,
        noteRepository: NoteRepository,
        taskRepository: TaskRepository,
        settingsRepository: SettingsRepository
    ) = ExportVathosDataUseCase(
        documentRepository,
        chatRepository,
        noteRepository,
        taskRepository,
        settingsRepository
    )
}


package com.vathos.app.di

import android.content.Context
import com.vathos.app.data.ai.EmbeddingGemmaEmbedder
import com.vathos.app.data.ai.EmbeddingModelDownloader
import com.vathos.app.data.ai.QwenLocalModelManager
import com.vathos.app.data.local.DatabaseProvider
import com.vathos.app.data.local.VathosDatabase
import com.vathos.app.data.repository.DocumentChunker
import com.vathos.app.domain.repository.LocalModelManager
import com.vathos.app.domain.usecase.RetrieveRelevantChunksUseCase
import com.vathos.app.domain.usecase.ToolCallingManager
import com.vathos.app.ui.viewmodel.ChatViewModelFactory
import com.vathos.app.ui.viewmodel.DocumentVaultViewModelFactory
import com.vathos.app.ui.viewmodel.HomeViewModelFactory
import com.vathos.app.ui.viewmodel.SettingsViewModelFactory

class VathosAppContainer(context: Context) {

    private val appContext = context.applicationContext

    private val database: VathosDatabase =
        DatabaseProvider.getDatabase(appContext)

    // Repositories
    val documentRepository =
        RepositoryProvider.provideDocumentRepository(database)

    val chatRepository =
        RepositoryProvider.provideChatRepository(database)

    val noteRepository =
        RepositoryProvider.provideNoteRepository(database)

    val taskRepository =
        RepositoryProvider.provideTaskRepository(database)

    val settingsRepository =
        RepositoryProvider.provideSettingsRepository(appContext)

    val pdfFileRepository =
        RepositoryProvider.providePdfFileRepository(appContext)

    val pdfTextRepository =
        RepositoryProvider.providePdfTextRepository(appContext)

    // PDF chunk repository
    val documentChunkRepository =
        RepositoryProvider.provideDocumentChunkRepository(database)

    // PDF chunking
    val documentChunker = DocumentChunker()

    // Qwen3 model manager
    val localModelManager: LocalModelManager =
        QwenLocalModelManager(appContext)

    // EmbeddingGemma model
    val embeddingModelDownloader =
        EmbeddingModelDownloader(
            appContext,
            settingsRepository
        )

    val embeddingGemmaEmbedder =
        EmbeddingGemmaEmbedder(
            appContext,
            embeddingModelDownloader
        )

    // Document use cases
    val getDocumentsUseCase =
        UseCaseProvider.provideGetDocuments(documentRepository)

    val searchDocumentsUseCase =
        UseCaseProvider.provideSearchDocuments(documentRepository)

    val addDocumentUseCase =
        UseCaseProvider.provideAddDocument(documentRepository)

    val deleteDocumentUseCase =
        UseCaseProvider.provideDeleteDocument(
            documentRepository,
            pdfFileRepository
        )

    val deleteAllDocumentsUseCase =
        UseCaseProvider.provideDeleteAllDocuments(
            documentRepository,
            pdfFileRepository
        )

    val getDocumentByIdUseCase =
        UseCaseProvider.provideGetDocumentById(documentRepository)

    val importPdfUseCase =
        UseCaseProvider.provideImportPdf(
            pdfFileRepository,
            documentRepository
        )

    val extractDocumentTextUseCase =
        UseCaseProvider.provideExtractDocumentText(
            documentRepository,
            pdfTextRepository
        )

    val generateDocumentSummaryUseCase =
        UseCaseProvider.provideGenerateDocumentSummary(
            documentRepository,
            pdfTextRepository,
            localModelManager
        )

    // PDF semantic retrieval
    val retrieveRelevantChunksUseCase =
        RetrieveRelevantChunksUseCase(
            documentChunkRepository,
            embeddingGemmaEmbedder
        )

    val indexDocumentChunksUseCase =
        UseCaseProvider.provideIndexDocumentChunks(
            extractDocumentTextUseCase,
            documentChunkRepository,
            documentChunker
        )

    // Chat use cases
    val getConversationMessagesUseCase =
        UseCaseProvider.provideGetConversationMessages(chatRepository)

    val getConversationCountUseCase =
        UseCaseProvider.provideGetConversationCount(chatRepository)

    val sendMessageUseCase =
        UseCaseProvider.provideSendMessage(chatRepository)

    val clearAllChatsUseCase =
        UseCaseProvider.provideClearAllChats(chatRepository)

    // Note use cases
    val createNoteUseCase =
        UseCaseProvider.provideCreateNote(noteRepository)

    val getAllNotesUseCase =
        UseCaseProvider.provideGetAllNotes(noteRepository)

    // Task use cases
    val createTaskUseCase =
        UseCaseProvider.provideCreateTask(taskRepository)

    val getAllTasksUseCase =
        UseCaseProvider.provideGetAllTasks(taskRepository)

    val getPendingTasksUseCase =
        UseCaseProvider.provideGetPendingTasks(taskRepository)

    val updateTaskStatusUseCase =
        UseCaseProvider.provideUpdateTaskStatus(taskRepository)

    val deleteTaskUseCase =
        UseCaseProvider.provideDeleteTask(taskRepository)

    val deleteAllTasksUseCase =
        UseCaseProvider.provideDeleteAllTasks(taskRepository)

    // Export use case
    val exportVathosDataUseCase =
        UseCaseProvider.provideExportVathosData(
            documentRepository = documentRepository,
            chatRepository = chatRepository,
            noteRepository = noteRepository,
            taskRepository = taskRepository,
            settingsRepository = settingsRepository
        )

    // Tool calling manager
    val toolCallingManager = ToolCallingManager(
        createNoteUseCase = createNoteUseCase,
        createTaskUseCase = createTaskUseCase,
        searchDocumentsUseCase = searchDocumentsUseCase,
        generateSummaryUseCase = generateDocumentSummaryUseCase
    )

    // ViewModel factories
    val documentVaultViewModelFactory =
        DocumentVaultViewModelFactory(
            getDocumentsUseCase,
            searchDocumentsUseCase,
            importPdfUseCase,
            deleteDocumentUseCase,
            generateDocumentSummaryUseCase
        )

    val homeViewModelFactory =
        HomeViewModelFactory(
            getDocumentsUseCase,
            getAllNotesUseCase,
            getAllTasksUseCase,
            getConversationCountUseCase
        )

    val chatViewModelFactory =
        ChatViewModelFactory(
            getConversationMessagesUseCase,
            sendMessageUseCase,
            generateDocumentSummaryUseCase,
            localModelManager,
            toolCallingManager,
            indexDocumentChunksUseCase,
            retrieveRelevantChunksUseCase,
            documentRepository
        )

    val settingsViewModelFactory =
        SettingsViewModelFactory(
            settingsRepository,
            clearAllChatsUseCase,
            deleteAllDocumentsUseCase,
            exportVathosDataUseCase
        )
}


package com.vathos.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vathos.app.domain.repository.DocumentRepository
import com.vathos.app.domain.repository.LocalModelManager
import com.vathos.app.domain.usecase.GenerateDocumentSummaryUseCase
import com.vathos.app.domain.usecase.GetConversationMessagesUseCase
import com.vathos.app.domain.usecase.IndexDocumentChunksUseCase
import com.vathos.app.domain.usecase.RetrieveRelevantChunksUseCase
import com.vathos.app.domain.usecase.SendMessageUseCase
import com.vathos.app.domain.usecase.ToolCallingManager

class ChatViewModelFactory(
    private val getMessagesUseCase: GetConversationMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val generateSummaryUseCase: GenerateDocumentSummaryUseCase,
    private val localModelManager: LocalModelManager,
    private val toolCallingManager: ToolCallingManager,
    private val indexDocumentChunksUseCase: IndexDocumentChunksUseCase,
    private val retrieveRelevantChunksUseCase: RetrieveRelevantChunksUseCase,
    private val documentRepository: DocumentRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            return ChatViewModel(
                getMessagesUseCase = getMessagesUseCase,
                sendMessageUseCase = sendMessageUseCase,
                generateSummaryUseCase = generateSummaryUseCase,
                localModelManager = localModelManager,
                toolCallingManager = toolCallingManager,
                indexDocumentChunksUseCase = indexDocumentChunksUseCase,
                retrieveRelevantChunksUseCase = retrieveRelevantChunksUseCase,
                documentRepository = documentRepository
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

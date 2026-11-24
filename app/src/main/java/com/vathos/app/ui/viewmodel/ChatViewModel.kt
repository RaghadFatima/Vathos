
package com.vathos.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vathos.app.data.ai.QwenLocalModelManager
import com.vathos.app.domain.model.ChatMessage
import com.vathos.app.domain.repository.DocumentRepository
import com.vathos.app.domain.repository.LocalModelManager
import com.vathos.app.domain.usecase.GenerateDocumentSummaryUseCase
import com.vathos.app.domain.usecase.GetConversationMessagesUseCase
import com.vathos.app.domain.usecase.IndexDocumentChunksUseCase
import com.vathos.app.domain.usecase.RetrieveRelevantChunksUseCase
import com.vathos.app.domain.usecase.SendMessageUseCase
import com.vathos.app.domain.usecase.ToolCallingManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ChatViewModel(
    private val getMessagesUseCase: GetConversationMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val generateSummaryUseCase: GenerateDocumentSummaryUseCase,
    private val localModelManager: LocalModelManager,
    private val toolCallingManager: ToolCallingManager,
    private val indexDocumentChunksUseCase: IndexDocumentChunksUseCase,
    private val retrieveRelevantChunksUseCase: RetrieveRelevantChunksUseCase,
    private val documentRepository: DocumentRepository
) : ViewModel() {

    private val conversationId = "main"

    private val qwenManager =
        localModelManager as? QwenLocalModelManager

    private val _isModelAvailable = MutableStateFlow(
        qwenManager?.isModelDownloaded() ?: true
    )

    val isModelAvailable: StateFlow<Boolean> =
        _isModelAvailable.asStateFlow()

    private val _messages =
        MutableStateFlow<List<ChatMessage>>(emptyList())

    val messages: StateFlow<List<ChatMessage>> =
        _messages.asStateFlow()

    private val _isTyping = MutableStateFlow(false)

    val isTyping: StateFlow<Boolean> =
        _isTyping.asStateFlow()

    private val _streamingResponse = MutableStateFlow("")

    val streamingResponse: StateFlow<String> =
        _streamingResponse.asStateFlow()

    private val _selectedDocumentId =
        MutableStateFlow<Long?>(null)

    val selectedDocumentId: StateFlow<Long?> =
        _selectedDocumentId.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)

    val error: StateFlow<String?> =
        _error.asStateFlow()

    init {
        viewModelScope.launch {
            getMessagesUseCase(conversationId).collect {
                _messages.value = it
            }
        }
    }

    fun refreshModelAvailability() {
        _isModelAvailable.value =
            qwenManager?.isModelDownloaded() ?: true
    }

    fun selectDocument(documentId: Long?) {
        _selectedDocumentId.value = documentId
    }

    fun sendMessage(text: String) {
        if (text.isBlank() || _isTyping.value) return

        refreshModelAvailability()

        if (!_isModelAvailable.value) {
            _error.value = "Download Model to Start Private Chat"
            return
        }

        val documentId = _selectedDocumentId.value

        viewModelScope.launch {
            _error.value = null
            _streamingResponse.value = ""
            _isTyping.value = true

            try {
                val cleanText = text.trim()

                val userMessage = ChatMessage(
                    id = 0,
                    conversationId = conversationId,
                    documentId = documentId,
                    message = cleanText,
                    isUser = true,
                    timestamp = System.currentTimeMillis()
                )

                sendMessageUseCase(userMessage)

                val response = createAssistantResponse(
                    userText = cleanText,
                    documentId = documentId
                )

                val assistantMessage = ChatMessage(
                    id = 0,
                    conversationId = conversationId,
                    documentId = documentId,
                    message = response,
                    isUser = false,
                    timestamp = System.currentTimeMillis()
                )

                sendMessageUseCase(assistantMessage)

            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _error.value =
                    exception.message ?: "Message could not be sent"
            } finally {
                _streamingResponse.value = ""
                _isTyping.value = false
            }
        }
    }

    private suspend fun createAssistantResponse(
        userText: String,
        documentId: Long?
    ): String {

        if (isNoteCreationRequest(userText)) {
            val note = extractNoteDetails(userText)

            if (note == null) {
                return "Please use this format: Create a note titled \"My Title\" with this content: Write your note here."
            }

            _streamingResponse.value = "Saving your note..."

            return toolCallingManager.createNoteDirect(
                title = note.first,
                content = note.second
            )
        }

        if (isTaskCreationRequest(userText)) {
            val task = extractTaskDetails(userText)

            if (task == null) {
                return "Please include a task title and due date. Example: Create a task titled \"Finish Report\" with due date \"2026-11-27\"."
            }

            _streamingResponse.value = "Saving your task..."

            return toolCallingManager.createTaskDirect(
                title = task.first,
                dueDate = task.second
            )
        }

        if (isDocumentSearchRequest(userText)) {
            val query = extractDocumentSearchQuery(userText)

            if (query.isNullOrBlank()) {
                return "Please tell me what document or information to search for. Example: Search my documents for \"business proposal\"."
            }

            _streamingResponse.value = "Searching your documents..."

            return toolCallingManager.searchDocumentsDirect(query)
        }

        if (isDocumentSummaryRequest(userText)) {
            if (documentId == null || documentId <= 0L) {
                return "Please select a document first so I can summarize it."
            }

            _streamingResponse.value =
                "Preparing your document summary..."

            return generateSummaryUseCase(documentId)
        }

        var documentContext = ""
        var documentName: String? = null

        if (documentId != null) {
            _streamingResponse.value =
                "Reading your selected document..."

            val document = documentRepository.getDocumentById(documentId)

            documentName = document?.name
                ?.takeIf { it.isNotBlank() }
                ?: "Selected PDF"

            val indexedCount =
                indexDocumentChunksUseCase(documentId)

            if (indexedCount <= 0) {
                return "I couldn't extract readable text from $documentName. Please check that it contains selectable text and try again."
            }

            val relevantChunks =
                retrieveRelevantChunksUseCase(
                    documentId = documentId,
                    query = userText,
                    maxResults = 3
                )

            documentContext = relevantChunks.joinToString("\n\n") {
                it.content
            }

            if (documentContext.isBlank()) {
                return "I couldn't find information relevant to your question in $documentName. Try asking about a topic or keyword that appears in the PDF."
            }
        }

        val prompt = buildModelPrompt(
            userText = userText,
            documentContext = documentContext,
            documentName = documentName
        )

        val modelOutput = StringBuilder()
        var isJsonOutput: Boolean? = null

        localModelManager
            .generateResponseStream(prompt)
            .collect { chunk ->
                modelOutput.append(chunk)

                val currentOutput = modelOutput.toString()

                if (isJsonOutput == null) {
                    val firstCharacter = currentOutput
                        .firstOrNull { !it.isWhitespace() }

                    if (firstCharacter != null) {
                        isJsonOutput = firstCharacter == '{'
                    }
                }

                if (isJsonOutput == false) {
                    _streamingResponse.value = currentOutput
                }
            }

        val completeOutput = modelOutput.toString().trim()

        if (completeOutput.isBlank()) {
            return "I couldn't generate a response. Please try again."
        }

        val toolResponse = toolCallingManager.executeModelOutput(
            modelOutput = completeOutput,
            selectedDocumentId = documentId
        )

        if (toolResponse != null) {
            _streamingResponse.value = toolResponse
            return toolResponse
        }

        val finalResponse = if (
            documentId != null &&
            !documentName.isNullOrBlank()
        ) {
            "$completeOutput\n\nSource: $documentName"
        } else {
            completeOutput
        }

        _streamingResponse.value = finalResponse
        return finalResponse
    }

    private fun isNoteCreationRequest(text: String): Boolean {
        return Regex(
            "\\b(create|save|write|add)\\b.*\\bnote\\b",
            RegexOption.IGNORE_CASE
        ).containsMatchIn(text)
    }

    private fun isTaskCreationRequest(text: String): Boolean {
        return Regex(
            "\\b(create|add|make)\\b.*\\b(task|reminder)\\b",
            RegexOption.IGNORE_CASE
        ).containsMatchIn(text)
    }

    private fun isDocumentSearchRequest(text: String): Boolean {
        return Regex(
            "\\b(search|find|look\\s+for)\\b.*\\b(documents?|pdfs?)\\b|" +
                    "\\b(which|what)\\s+(documents?|pdfs?)\\b",
            RegexOption.IGNORE_CASE
        ).containsMatchIn(text)
    }

    private fun extractDocumentSearchQuery(text: String): String? {
        val marker = Regex(
            "\\b(?:for|about|containing|mention(?:s)?)\\s+(.+)$",
            RegexOption.IGNORE_CASE
        ).find(text) ?: return null

        val query = marker.groupValues[1]
            .trim()
            .trim('"', '\'')
            .trimEnd('.', '?', '!')

        return query.takeIf { it.isNotBlank() }
    }

    private fun extractNoteDetails(text: String): Pair<String, String>? {
        val contentMarker = Regex(
            "\\bwith\\s+(?:this\\s+)?content\\s*[:=]?\\s*|\\bcontent\\s*[:=]\\s*",
            RegexOption.IGNORE_CASE
        )

        val marker = contentMarker.find(text) ?: return null

        val beforeContent = text.substring(0, marker.range.first)
        val content = text.substring(marker.range.last + 1)
            .trim()
            .trim('"', '\'')

        val titleMatch = Regex(
            "\\btitle(?:d)?\\s*(?:is\\s*)?[:=]?\\s*(.+)$",
            RegexOption.IGNORE_CASE
        ).find(beforeContent) ?: return null

        val title = titleMatch.groupValues[1]
            .trim()
            .trim('"', '\'')
            .trimEnd('.', ':')

        if (title.isBlank() || content.isBlank()) {
            return null
        }

        return title to content
    }

    private fun extractTaskDetails(text: String): Pair<String, String>? {
        val dueDateMarker = Regex(
            "\\b(?:with\\s+)?due\\s+date\\s*[:=]?\\s*",
            RegexOption.IGNORE_CASE
        )

        val marker = dueDateMarker.find(text) ?: return null

        val beforeDate = text.substring(0, marker.range.first)
        val dueDate = text.substring(marker.range.last + 1)
            .trim()
            .trim('"', '\'')
            .trimEnd('.', '!')

        val titleMatch = Regex(
            "\\btitle(?:d)?\\s*(?:is\\s*)?[:=]?\\s*(.+)$",
            RegexOption.IGNORE_CASE
        ).find(beforeDate) ?: return null

        val title = titleMatch.groupValues[1]
            .trim()
            .trim('"', '\'')
            .trimEnd('.', ':')

        if (title.isBlank() || dueDate.isBlank()) {
            return null
        }

        return title to dueDate
    }

    private fun isDocumentSummaryRequest(text: String): Boolean {
        return Regex(
            "\\b(summarize|summarise|summary)\\b",
            RegexOption.IGNORE_CASE
        ).containsMatchIn(text)
    }

    private fun buildModelPrompt(
        userText: String,
        documentContext: String,
        documentName: String?
    ): String {

        val documentInstructions =
            if (documentContext.isNotBlank()) {
                """
                The user selected a document: $documentName.

                Answer using only the document text below.
                Do not invent information or use outside knowledge.
                If the answer is not contained in the provided text,
                clearly say that the document does not provide that information.
                Give exactly three concise bullet points.
                Use complete, grammatically correct sentences.

                DOCUMENT TEXT:
                $documentContext
                """.trimIndent()
            } else {
                ""
            }

        return """
            You are Vathos, a helpful private AI assistant
            running locally on the user's device.

            Answer the user's actual question directly.
            Do not give a generic greeting instead of an answer.
            Use clear, natural English.
            Be accurate and concise.
            If you do not know something, say so.
            Do not invent facts.

            $documentInstructions

            USER QUESTION:
            $userText

            ANSWER:
        """.trimIndent()
    }

    fun clearError() {
        _error.value = null
    }
}

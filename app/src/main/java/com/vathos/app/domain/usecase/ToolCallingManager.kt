
package com.vathos.app.domain.usecase

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import org.json.JSONObject

class ToolCallingManager(
    private val createNoteUseCase: CreateNoteUseCase,
    private val createTaskUseCase: CreateTaskUseCase,
    private val searchDocumentsUseCase: SearchDocumentsUseCase,
    private val generateSummaryUseCase: GenerateDocumentSummaryUseCase
) {

    suspend fun createNoteDirect(
        title: String,
        content: String
    ): String {
        if (title.isBlank() || content.isBlank()) {
            return "I need both a title and content to create a note."
        }

        return try {
            createNoteUseCase(title.trim(), content.trim())
            "Note created successfully!\n\nTitle: ${title.trim()}"
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            "I couldn't save the note. ${exception.message ?: "Please try again."}"
        }
    }

    suspend fun createTaskDirect(
        title: String,
        dueDate: String
    ): String {
        if (title.isBlank()) {
            return "I need a title to create a task."
        }

        if (dueDate.isBlank()) {
            return "Please provide a due date for this task."
        }

        return try {
            createTaskUseCase(title.trim(), dueDate.trim())
            "Task created successfully!\n\nTitle: ${title.trim()}\nDue: ${dueDate.trim()}"
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            "I couldn't save the task. ${exception.message ?: "Please try again."}"
        }
    }

    suspend fun searchDocumentsDirect(query: String): String {
        if (query.isBlank()) {
            return "Please tell me what document or information to search for."
        }

        return try {
            val results = searchDocumentsUseCase(query.trim()).first()

            if (results.isEmpty()) {
                "I couldn't find any documents matching \"$query\". " +
                        "Try another search phrase."
            } else {
                val matches = results.joinToString("\n") { document ->
                    "• ${document.name}"
                }

                """
                I found ${results.size} matching document(s):

                $matches
                """.trimIndent()
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            "I couldn't search your documents. " +
                    (exception.message ?: "Please try again.")
        }
    }

    suspend fun executeModelOutput(
        modelOutput: String,
        selectedDocumentId: Long?
    ): String? {
        val json = extractJson(modelOutput) ?: return null

        return try {
            val toolName = json.optString("tool").trim()
            val params = json.optJSONObject("params") ?: JSONObject()

            when (toolName) {
                "create_note" -> createNote(params)
                "create_task" -> createTask(params)
                "summarize_document" ->
                    summarizeDocument(params, selectedDocumentId)
                "search_documents" -> searchDocuments(params)
                else -> null
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            "I couldn't complete that action. " +
                    (exception.message?.takeIf { it.isNotBlank() }
                        ?: "Please try again.")
        }
    }

    private suspend fun createNote(params: JSONObject): String {
        val title = params.optString("title").trim()
        val content = params.optString("content").trim()

        return createNoteDirect(title, content)
    }

    private suspend fun createTask(params: JSONObject): String {
        val title = params.optString("title").trim()

        val dueDate = params.optString(
            "due_date",
            params.optString("dueDate")
        ).trim()

        return createTaskDirect(title, dueDate)
    }

    private suspend fun summarizeDocument(
        params: JSONObject,
        selectedDocumentId: Long?
    ): String {
        val requestedId = params.optLong("document_id", 0L)

        val documentId = when {
            selectedDocumentId != null -> selectedDocumentId
            requestedId > 0L -> requestedId
            else -> null
        }

        if (documentId == null || documentId <= 0L) {
            return "Please select a document first so I can summarize it."
        }

        return generateSummaryUseCase(documentId)
    }

    private suspend fun searchDocuments(params: JSONObject): String {
        val query = params.optString("query").trim()
        return searchDocumentsDirect(query)
    }

    private fun extractJson(output: String): JSONObject? {
        val start = output.indexOf('{')
        val end = output.lastIndexOf('}')

        if (start < 0 || end <= start) {
            return null
        }

        return try {
            JSONObject(output.substring(start, end + 1))
        } catch (_: Exception) {
            null
        }
    }
}

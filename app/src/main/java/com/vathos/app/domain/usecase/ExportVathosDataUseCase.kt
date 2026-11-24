
package com.vathos.app.domain.usecase

import com.vathos.app.domain.repository.ChatRepository
import com.vathos.app.domain.repository.DocumentRepository
import com.vathos.app.domain.repository.NoteRepository
import com.vathos.app.domain.repository.SettingsRepository
import com.vathos.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

class ExportVathosDataUseCase(
    private val documentRepository: DocumentRepository,
    private val chatRepository: ChatRepository,
    private val noteRepository: NoteRepository,
    private val taskRepository: TaskRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(): String {
        val documents = documentRepository.getAllDocuments().first()
        val messages = chatRepository.getAllMessages().first()
        val notes = noteRepository.getAllNotes().first()
        val tasks = taskRepository.getAllTasks().first()
        val preferences = settingsRepository.preferences.first()

        val root = JSONObject().apply {
            put("app", "Vathos")
            put("schemaVersion", 1)
            put("exportedAt", System.currentTimeMillis())

            put("preferences", JSONObject().apply {
                put("offlineMode", preferences.offlineMode)
                put("theme", preferences.theme.name)
            })

            put("documents", JSONArray().apply {
                documents.forEach { document ->
                    put(JSONObject().apply {
                        put("id", document.id)
                        put("name", document.name)
                        put("size", document.size)
                        put("date", document.date)
                        put("pages", document.pages)
                        put("summary", document.summary)
                    })
                }
            })

            put("chats", JSONArray().apply {
                messages.forEach { message ->
                    put(JSONObject().apply {
                        put("id", message.id)
                        put("conversationId", message.conversationId)
                        put("documentId", message.documentId ?: JSONObject.NULL)
                        put("message", message.message)
                        put("isUser", message.isUser)
                        put("timestamp", message.timestamp)
                    })
                }
            })

            put("notes", JSONArray().apply {
                notes.forEach { note ->
                    put(JSONObject().apply {
                        put("id", note.id)
                        put("title", note.title)
                        put("content", note.content)
                        put("createdAt", note.createdAt)
                    })
                }
            })

            put("tasks", JSONArray().apply {
                tasks.forEach { task ->
                    put(JSONObject().apply {
                        put("id", task.id)
                        put("title", task.title)
                        put("dueDate", task.dueDate)
                        put("isCompleted", task.isCompleted)
                        put("createdAt", task.createdAt)
                    })
                }
            })
        }

        return root.toString(2)
    }
}

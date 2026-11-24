
package com.vathos.app.data.ai

import com.vathos.app.domain.repository.LocalModelManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DemoLocalModelManager : LocalModelManager {

    override suspend fun generateResponse(prompt: String): String {
        val normalized = prompt.lowercase()

        return when {
            normalized.contains("hello") ||
                    normalized.contains("hi") ->
                "Hello! I'm Vathos, your private business assistant. How can I help you today?"

            normalized.contains("sales") ->
                "I can help analyze your sales performance. Please select a sales report or share the figures you'd like me to examine."

            normalized.contains("business") ->
                "I can help with business documents, reports, tasks, and notes. What would you like to work on?"

            normalized.contains("help") ->
                "I can assist with document summaries, business information, notes, and tasks. Select a document to get started."

            else ->
                "I've received your message: \"$prompt\"\n\nI'm currently using demonstration mode. The real on-device language model will be connected in the next integration phase."
        }
    }

    override fun generateResponseStream(prompt: String): Flow<String> = flow {
        emit(generateResponse(prompt))
    }
}

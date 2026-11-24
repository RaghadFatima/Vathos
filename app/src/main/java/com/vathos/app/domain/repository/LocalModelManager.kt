
package com.vathos.app.domain.repository

import kotlinx.coroutines.flow.Flow

interface LocalModelManager {

    suspend fun generateResponse(prompt: String): String

    fun generateResponseStream(prompt: String): Flow<String>

    /**
     * Generates each response in a separate conversation.
     * This prevents one PDF section from filling the context
     * needed to summarize the next section.
     */
    suspend fun generateResponsesInFreshConversations(
        prompts: List<String>
    ): List<String> {
        return prompts.map { prompt ->
            generateResponse(prompt)
        }
    }
}

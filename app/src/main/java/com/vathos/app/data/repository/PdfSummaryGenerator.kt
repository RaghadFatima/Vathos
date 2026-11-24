
package com.vathos.app.data.repository

import com.vathos.app.domain.repository.LocalModelManager

class PdfSummaryGenerator(
    private val localModelManager: LocalModelManager
) {

    // Larger chunks reduce the number of model generations.
    private val wordsPerChunk = 650

    suspend fun generateSummary(
        extractedText: String,
        maxSentences: Int = 5
    ): String {

        if (extractedText.isBlank()) {
            return "No readable text was found in this PDF."
        }

        val cleanedText = extractedText
            .replace(Regex("\\s+"), " ")
            .trim()

        val chunks = splitIntoChunks(cleanedText)

        if (chunks.isEmpty()) {
            return "No readable text was found in this PDF."
        }

        // Generate one concise summary per document section.
        val sectionPrompts = chunks.mapIndexed { index, chunk ->

            """
            Summarize section ${index + 1} of ${chunks.size}.

            Instructions:
            - Identify the most important facts and ideas.
            - Preserve important names, dates and conclusions.
            - Do not invent information.
            - Avoid repetition.
            - Return 1 or 2 concise bullet points.
            - Use clear, complete sentences.

            DOCUMENT SECTION:
            $chunk
            """.trimIndent()
        }

        val summaries = localModelManager
            .generateResponsesInFreshConversations(sectionPrompts)
            .filter { it.isNotBlank() }

        if (summaries.isEmpty()) {
            return "The PDF could not be summarized."
        }

        // No additional grouping calls are needed.
        // Combine all section summaries in one final generation.
        if (summaries.size == 1) {
            return summaries.first()
        }

        val finalPrompt = """
            Create one accurate summary of the entire PDF
            using the section summaries below.

            Requirements:
            - Cover the important information from all sections.
            - Include the main ideas and conclusions.
            - Preserve important facts.
            - Remove repeated information.
            - Do not invent facts.
            - Use clear, natural English.
            - Write no more than $maxSentences sentences.

            SECTION SUMMARIES:
            ${summaries.joinToString("\n\n")}
        """.trimIndent()

        return localModelManager
            .generateResponsesInFreshConversations(listOf(finalPrompt))
            .firstOrNull()
            ?.takeIf { it.isNotBlank() }
            ?: "The PDF could not be summarized."
    }

    private fun splitIntoChunks(text: String): List<String> {

        val words = text
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }

        if (words.isEmpty()) {
            return emptyList()
        }

        val chunks = mutableListOf<String>()

        var start = 0

        while (start < words.size) {

            val end = minOf(
                start + wordsPerChunk,
                words.size
            )

            chunks.add(
                words.subList(start, end).joinToString(" ")
            )

            start = end
        }

        return chunks
    }
}

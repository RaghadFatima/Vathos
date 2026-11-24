
package com.vathos.app.domain.usecase

import com.vathos.app.data.ai.EmbeddingGemmaEmbedder
import com.vathos.app.data.local.entity.DocumentChunkEntity
import com.vathos.app.domain.repository.DocumentChunkRepository
import kotlinx.coroutines.CancellationException
import kotlin.math.sqrt

class RetrieveRelevantChunksUseCase(
    private val documentChunkRepository: DocumentChunkRepository,
    private val embeddingGemmaEmbedder: EmbeddingGemmaEmbedder
) {

    companion object {
        private const val MIN_SEMANTIC_SCORE = 0.15
    }

    private val ignoredWords = setOf(
        "the", "and", "for", "that", "this",
        "with", "from", "what", "when", "where",
        "which", "who", "why", "how", "does",
        "did", "are", "was", "were", "have",
        "has", "had", "can", "could", "would",
        "should", "about", "into", "their",
        "there", "them", "they", "then",
        "you", "your", "our", "its",
        "document", "please", "tell", "explain",
        "give", "show", "find", "information"
    )

    suspend operator fun invoke(
        documentId: Long,
        query: String,
        maxResults: Int = 3
    ): List<DocumentChunkEntity> {

        require(documentId > 0) {
            "Invalid document ID"
        }

        if (query.isBlank() || maxResults <= 0) {
            return emptyList()
        }

        val chunks = documentChunkRepository.getChunks(documentId)

        if (chunks.isEmpty()) {
            return emptyList()
        }

        return try {
            retrieveSemantically(
                chunks = chunks,
                query = query,
                maxResults = maxResults
            )
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Exception) {
            retrieveByKeywords(
                chunks = chunks,
                query = query,
                maxResults = maxResults
            )
        }
    }

    private suspend fun retrieveSemantically(
        chunks: List<DocumentChunkEntity>,
        query: String,
        maxResults: Int
    ): List<DocumentChunkEntity> {

        val queryEmbedding =
            embeddingGemmaEmbedder.embedQuery(query)

        val embeddedChunks = chunks.map { chunk ->

            val existingVector = parseEmbedding(chunk.embedding)

            val vector = if (existingVector.isNotEmpty()) {
                existingVector
            } else {
                val generated =
                    embeddingGemmaEmbedder.embedDocument(chunk.content)

                documentChunkRepository.updateEmbedding(
                    chunkId = chunk.id,
                    embedding = generated.joinToString(",")
                )

                generated
            }

            chunk to vector
        }

        val ranked = embeddedChunks
            .mapNotNull { (chunk, vector) ->

                val score = cosineSimilarity(
                    queryEmbedding,
                    vector
                ) ?: return@mapNotNull null

                chunk to score
            }
            .sortedByDescending { it.second }

        if (ranked.isEmpty()) {
            return retrieveByKeywords(
                chunks,
                query,
                maxResults
            )
        }

        if (ranked.first().second < MIN_SEMANTIC_SCORE) {
            return retrieveByKeywords(
                chunks,
                query,
                maxResults
            )
        }

        return ranked
            .take(maxResults)
            .map { it.first }
    }

    private fun retrieveByKeywords(
        chunks: List<DocumentChunkEntity>,
        query: String,
        maxResults: Int
    ): List<DocumentChunkEntity> {

        val queryTerms = query
            .lowercase()
            .split(Regex("[^a-z0-9]+"))
            .filter { it.length > 2 }
            .filterNot { it in ignoredWords }
            .toSet()

        if (queryTerms.isEmpty()) {
            return emptyList()
        }

        return chunks
            .map { chunk ->

                val chunkTerms = chunk.content
                    .lowercase()
                    .split(Regex("[^a-z0-9]+"))
                    .toSet()

                val score = queryTerms.count {
                    it in chunkTerms
                }

                chunk to score
            }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .take(maxResults)
            .map { it.first }
    }

    private fun parseEmbedding(value: String): List<Float> {
        if (value.isBlank()) return emptyList()

        return value.split(",").mapNotNull {
            it.toFloatOrNull()
        }
    }

    private fun cosineSimilarity(
        first: List<Float>,
        second: List<Float>
    ): Double? {

        if (first.isEmpty() || first.size != second.size) {
            return null
        }

        var dotProduct = 0.0
        var firstMagnitude = 0.0
        var secondMagnitude = 0.0

        for (index in first.indices) {
            val a = first[index].toDouble()
            val b = second[index].toDouble()

            dotProduct += a * b
            firstMagnitude += a * a
            secondMagnitude += b * b
        }

        val denominator =
            sqrt(firstMagnitude) * sqrt(secondMagnitude)

        if (denominator == 0.0) return null

        return dotProduct / denominator
    }
}

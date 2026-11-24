
package com.vathos.app.data.ai

import android.content.Context
import com.google.mediapipe.tasks.text.textembedder.TextEmbedder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

class EmbeddingGemmaEmbedder(
    context: Context,
    private val downloader: EmbeddingModelDownloader
) {

    private val appContext = context.applicationContext
    private val mutex = Mutex()

    private var embedder: TextEmbedder? = null
    private var loadedModelPath: String? = null

    suspend fun embedDocument(text: String): List<Float> {
        require(text.isNotBlank()) {
            "Cannot embed empty document text."
        }

        return embed(
            "title: none | text: $text"
        )
    }

    suspend fun embedQuery(query: String): List<Float> {
        require(query.isNotBlank()) {
            "Cannot embed an empty search query."
        }

        return embed(
            "task: search result | query: $query"
        )
    }

    private suspend fun embed(input: String): List<Float> =
        mutex.withLock {
            withContext(Dispatchers.Default) {

                val modelFile: File = downloader.getModelFile()
                    ?: throw IllegalStateException(
                        "Download EmbeddingGemma before using semantic search."
                    )

                val modelPath = modelFile.absolutePath

                if (embedder == null || loadedModelPath != modelPath) {
                    embedder?.close()

                    embedder = TextEmbedder.createFromFile(
                        appContext,
                        modelFile
                    )

                    loadedModelPath = modelPath
                }

                val result = requireNotNull(embedder)
                    .embed(input)

                val vector = result
                    .embeddingResult()
                    .embeddings()
                    .firstOrNull()
                    ?.floatEmbedding()
                    ?.toList()
                    ?: emptyList()

                check(vector.isNotEmpty()) {
                    "EmbeddingGemma returned an empty embedding."
                }

                vector
            }
        }

    suspend fun close() {
        mutex.withLock {
            withContext(Dispatchers.IO) {
                embedder?.close()
                embedder = null
                loadedModelPath = null
            }
        }
    }
}

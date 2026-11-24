
package com.vathos.app.data.ai

import android.content.Context
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.SamplerConfig
import com.vathos.app.domain.repository.LocalModelManager
import java.io.File
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class QwenLocalModelManager(context: Context) : LocalModelManager {

    private val appContext = context.applicationContext

    private val modelFile = File(
        appContext.filesDir,
        "models/qwen3_0.6b_nothink_q4_block32_ekv1280.litertlm"
    )

    private var engine: Engine? = null
    private var conversation: Conversation? = null

    private val mutex = Mutex()

    private val _tokensPerSecond = MutableStateFlow(0)

    val tokensPerSecond: StateFlow<Int> =
        _tokensPerSecond.asStateFlow()

    /*
     * Estimates generation speed using approximately
     * four characters per token.
     *
     * Uses longer measurement windows and smoothing
     * to reduce sudden fluctuations.
     */
    private inner class SpeedTracker {

        private var generationStart = 0L
        private var windowStart = 0L

        private var windowCharacters = 0
        private var smoothedSpeed = 0.0

        private val measurementWindowNanos =
            1_500_000_000L

        private val smoothingFactor = 0.20

        fun start() {
            val now = System.nanoTime()

            generationStart = now
            windowStart = now

            windowCharacters = 0
            smoothedSpeed = 0.0

            _tokensPerSecond.value = 0
        }

        fun add(text: String) {
            if (text.isEmpty()) return

            windowCharacters += text.length

            val now = System.nanoTime()
            val elapsedNanos = now - windowStart

            if (elapsedNanos >= measurementWindowNanos) {

                val elapsedSeconds =
                    elapsedNanos / 1_000_000_000.0

                val estimatedTokens =
                    windowCharacters / 4.0

                val recentSpeed =
                    estimatedTokens / elapsedSeconds

                smoothedSpeed =
                    if (smoothedSpeed == 0.0) {
                        recentSpeed
                    } else {
                        (smoothedSpeed * (1.0 - smoothingFactor)) +
                                (recentSpeed * smoothingFactor)
                    }

                _tokensPerSecond.value =
                    smoothedSpeed
                        .roundToInt()
                        .coerceAtLeast(1)

                windowCharacters = 0
                windowStart = now
            }
        }

        fun finish() {
            val now = System.nanoTime()

            val totalElapsedNanos =
                now - generationStart

            val totalElapsedSeconds =
                totalElapsedNanos / 1_000_000_000.0

            if (totalElapsedSeconds > 0.0) {
                // Include the final partial measurement window.
                // This prevents short generations from remaining at zero.
                val finalWindowSeconds =
                    (now - windowStart) / 1_000_000_000.0

                if (windowCharacters > 0 && finalWindowSeconds > 0.0) {
                    val finalWindowSpeed =
                        (windowCharacters / 4.0) / finalWindowSeconds

                    smoothedSpeed =
                        if (smoothedSpeed == 0.0) {
                            finalWindowSpeed
                        } else {
                            (smoothedSpeed * (1.0 - smoothingFactor)) +
                                    (finalWindowSpeed * smoothingFactor)
                        }

                    _tokensPerSecond.value =
                        smoothedSpeed
                            .roundToInt()
                            .coerceAtLeast(1)
                }
            }

            generationStart = 0L
            windowStart = 0L
            windowCharacters = 0
            smoothedSpeed = 0.0
        }
    }

    private fun createConversationConfig(): ConversationConfig {
        return ConversationConfig(
            systemInstruction = Contents.of(
                "You are Vathos, a helpful private on-device AI assistant. " +
                        "Give clear, accurate and concise answers."
            ),
            samplerConfig = SamplerConfig(
                temperature = 0.7,
                topK = 40,
                topP = 0.9
            )
        )
    }

    private suspend fun initializeModel() {
        if (conversation != null) return

        if (!modelFile.exists() || modelFile.length() <= 0L) {
            throw IllegalStateException(
                "Qwen3-0.6B is not downloaded. Please download the model first."
            )
        }

        withContext(Dispatchers.IO) {
            val newEngine = Engine(
                EngineConfig(
                    modelPath = modelFile.absolutePath,
                    backend = Backend.CPU(),
                    cacheDir = appContext.cacheDir.absolutePath
                )
            )

            try {
                newEngine.initialize()

                val newConversation =
                    newEngine.createConversation(
                        createConversationConfig()
                    )

                engine = newEngine
                conversation = newConversation

            } catch (exception: Exception) {
                newEngine.close()
                throw exception
            }
        }
    }

    override fun generateResponseStream(
        prompt: String
    ): Flow<String> = flow {

        require(prompt.isNotBlank()) {
            "Prompt cannot be empty."
        }

        mutex.withLock {
            initializeModel()

            val activeConversation = conversation
                ?: throw IllegalStateException(
                    "Qwen3 conversation could not be initialized."
                )

            val speedTracker = SpeedTracker()
            speedTracker.start()

            try {
                activeConversation.sendMessageAsync(
                    text = prompt,
                    maxOutputToken = 512
                ).collect { message ->

                    val chunk = message.toString()

                    if (chunk.isNotEmpty()) {
                        emit(chunk)
                        speedTracker.add(chunk)
                    }
                }

            } finally {
                speedTracker.finish()
            }
        }

    }.flowOn(Dispatchers.IO)

    override suspend fun generateResponse(
        prompt: String
    ): String {
        val response = StringBuilder()

        generateResponseStream(prompt).collect { chunk ->
            response.append(chunk)
        }

        return response.toString().trim()
    }

    override suspend fun generateResponsesInFreshConversations(
        prompts: List<String>
    ): List<String> {

        require(prompts.all { it.isNotBlank() }) {
            "All prompts must contain text."
        }

        if (prompts.isEmpty()) return emptyList()

        return mutex.withLock {
            initializeModel()

            val activeEngine = engine
                ?: throw IllegalStateException(
                    "Qwen3 engine could not be initialized."
                )

            val speedTracker = SpeedTracker()
            speedTracker.start()

            try {
                withContext(Dispatchers.IO) {
                    prompts.map { prompt ->

                        val isolatedConversation =
                            activeEngine.createConversation(
                                createConversationConfig()
                            )

                        try {
                            val response = StringBuilder()

                            isolatedConversation.sendMessageAsync(
                                text = prompt,
                                maxOutputToken = 512
                            ).collect { message ->

                                val chunk = message.toString()

                                response.append(chunk)
                                speedTracker.add(chunk)
                            }

                            response.toString().trim()

                        } finally {
                            isolatedConversation.close()
                        }
                    }
                }

            } finally {
                speedTracker.finish()
            }
        }
    }

    suspend fun closeModel() {
        mutex.withLock {
            withContext(Dispatchers.IO) {
                conversation?.close()
                conversation = null

                engine?.close()
                engine = null

                _tokensPerSecond.value = 0
            }
        }
    }

    fun isModelDownloaded(): Boolean {
        return modelFile.exists() && modelFile.length() > 0L
    }

    fun getModelFile(): File {
        return modelFile
    }
}

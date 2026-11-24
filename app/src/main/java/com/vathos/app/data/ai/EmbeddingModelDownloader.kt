
package com.vathos.app.data.ai

import android.content.Context
import com.vathos.app.domain.repository.SettingsRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

data class EmbeddingDownloadState(
    val isDownloading: Boolean = false,
    val isDownloaded: Boolean = false,
    val downloadedBytes: Long = 0L,
    val totalBytes: Long = -1L,
    val error: String? = null
) {
    val progress: Int
        get() = if (totalBytes > 0L) {
            ((downloadedBytes * 100L) / totalBytes)
                .toInt()
                .coerceIn(0, 100)
        } else {
            0
        }
}

class EmbeddingModelDownloader(
    context: Context,
    private val settingsRepository: SettingsRepository
) {
    companion object {
        private const val MODEL_URL =
            "https://storage.googleapis.com/mediapipe-models/text_embedder/embedding_gemma/int4int8/latest/embedding_gemma.task"

        private const val BUFFER_SIZE = 16 * 1024
        private const val CONNECT_TIMEOUT = 30_000
        private const val READ_TIMEOUT = 60_000
    }

    private val appContext = context.applicationContext

    private val modelDirectory =
        File(appContext.filesDir, "models")

    private val modelFile =
        File(modelDirectory, "embedding_gemma.task")

    private val temporaryFile =
        File(modelDirectory, "embedding_gemma.task.download")

    private val downloadMutex = Mutex()

    private val networkAccessPolicy =
        NetworkAccessPolicy(settingsRepository)

    private val _state = MutableStateFlow(
        EmbeddingDownloadState(
            isDownloaded = modelFile.exists() && modelFile.length() > 0L,
            downloadedBytes = modelFile.length(),
            totalBytes = if (modelFile.exists()) modelFile.length() else -1L
        )
    )

    val state: StateFlow<EmbeddingDownloadState> =
        _state.asStateFlow()

    val isModelDownloaded: Boolean
        get() = modelFile.exists() && modelFile.length() > 0L

    fun getModelFile(): File? {
        return modelFile.takeIf {
            it.exists() && it.length() > 0L
        }
    }

    suspend fun download(): File = downloadMutex.withLock {
        withContext(Dispatchers.IO) {

            if (isModelDownloaded) {
                return@withContext modelFile
            }

            networkAccessPolicy.ensureNetworkAllowed()

            modelDirectory.mkdirs()
            temporaryFile.delete()

            _state.value = EmbeddingDownloadState(
                isDownloading = true
            )

            var connection: HttpURLConnection? = null

            try {
                networkAccessPolicy.ensureNetworkAllowed()

                connection = (URL(MODEL_URL).openConnection()
                        as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = CONNECT_TIMEOUT
                    readTimeout = READ_TIMEOUT
                    instanceFollowRedirects = true
                    setRequestProperty(
                        "User-Agent",
                        "Vathos-Android"
                    )
                }

                connection.connect()

                val responseCode = connection.responseCode

                if (responseCode !in 200..299) {
                    throw IllegalStateException(
                        "Model download failed. HTTP $responseCode"
                    )
                }

                val totalBytes = connection.contentLengthLong
                var downloadedBytes = 0L

                connection.inputStream.use { input ->
                    FileOutputStream(temporaryFile).use { output ->
                        val buffer = ByteArray(BUFFER_SIZE)

                        while (true) {
                            networkAccessPolicy.ensureNetworkAllowed()

                            val bytesRead = input.read(buffer)

                            if (bytesRead == -1) {
                                break
                            }

                            output.write(buffer, 0, bytesRead)
                            downloadedBytes += bytesRead

                            _state.value = EmbeddingDownloadState(
                                isDownloading = true,
                                downloadedBytes = downloadedBytes,
                                totalBytes = totalBytes
                            )
                        }

                        output.fd.sync()
                    }
                }

                if (temporaryFile.length() == 0L) {
                    throw IllegalStateException(
                        "The downloaded model file is empty."
                    )
                }

                if (
                    totalBytes > 0L &&
                    temporaryFile.length() != totalBytes
                ) {
                    throw IllegalStateException(
                        "The model download was incomplete. Please try again."
                    )
                }

                if (modelFile.exists()) {
                    modelFile.delete()
                }

                if (!temporaryFile.renameTo(modelFile)) {
                    throw IllegalStateException(
                        "Could not save the downloaded model."
                    )
                }

                _state.value = EmbeddingDownloadState(
                    isDownloaded = true,
                    downloadedBytes = modelFile.length(),
                    totalBytes = modelFile.length()
                )

                modelFile

            } catch (exception: CancellationException) {
                temporaryFile.delete()

                _state.value = EmbeddingDownloadState(
                    isDownloaded = isModelDownloaded
                )

                throw exception

            } catch (exception: Exception) {
                temporaryFile.delete()

                _state.value = EmbeddingDownloadState(
                    isDownloaded = isModelDownloaded,
                    error = exception.message
                        ?: "Unable to download the embedding model."
                )

                throw exception

            } finally {
                connection?.disconnect()
            }
        }
    }

    suspend fun deleteModel(): Boolean =
        withContext(Dispatchers.IO) {
            temporaryFile.delete()

            val deleted = !modelFile.exists() || modelFile.delete()

            _state.value = EmbeddingDownloadState(
                isDownloaded = modelFile.exists() &&
                        modelFile.length() > 0L,
                downloadedBytes = modelFile.length(),
                totalBytes = if (modelFile.exists()) {
                    modelFile.length()
                } else {
                    -1L
                }
            )

            deleted
        }
}


package com.vathos.app.data.ai

import android.content.Context
import com.vathos.app.data.repository.SettingsRepositoryImpl
import com.vathos.app.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

data class ModelDownloadProgress(
    val downloadedBytes: Long = 0L,
    val totalBytes: Long = 0L,
    val isDownloading: Boolean = false,
    val isComplete: Boolean = false,
    val error: String? = null
) {
    val downloadedMb: Double
        get() = downloadedBytes / (1024.0 * 1024.0)

    val totalMb: Double
        get() = totalBytes / (1024.0 * 1024.0)

    val percentage: Int
        get() = if (totalBytes > 0L) {
            ((downloadedBytes * 100L) / totalBytes)
                .toInt()
                .coerceIn(0, 100)
        } else {
            0
        }
}

class QwenModelDownloader(
    context: Context,
    private val settingsRepository: SettingsRepository =
        SettingsRepositoryImpl(context.applicationContext)
) {

    private val appContext = context.applicationContext

    private val modelDirectory =
        File(appContext.filesDir, "models")

    private val modelFile = File(
        modelDirectory,
        "qwen3_0.6b_nothink_q4_block32_ekv1280.litertlm"
    )

    private val temporaryFile =
        File(modelDirectory, "qwen3_model.download")

    private val networkAccessPolicy =
        NetworkAccessPolicy(settingsRepository)

    private val _progress =
        MutableStateFlow(ModelDownloadProgress())

    val progress: StateFlow<ModelDownloadProgress> =
        _progress

    private val modelUrl =
        "https://huggingface.co/litert-community/Qwen3-0.6B-int4/resolve/main/qwen3_0.6b_nothink_q4_block32_ekv1280.litertlm"

    fun isModelDownloaded(): Boolean {
        return modelFile.exists() &&
                modelFile.isFile &&
                modelFile.length() > 0L
    }

    fun getModelFile(): File {
        return modelFile
    }

    suspend fun downloadModel() = withContext(Dispatchers.IO) {

        try {
            networkAccessPolicy.ensureNetworkAllowed()

            if (isModelDownloaded()) {
                _progress.value = ModelDownloadProgress(
                    downloadedBytes = modelFile.length(),
                    totalBytes = modelFile.length(),
                    isComplete = true
                )
                return@withContext
            }

            modelDirectory.mkdirs()
            temporaryFile.delete()

            _progress.value = ModelDownloadProgress(
                isDownloading = true
            )

            var connection: HttpURLConnection? = null

            try {
                networkAccessPolicy.ensureNetworkAllowed()

                connection =
                    (URL(modelUrl).openConnection() as HttpURLConnection).apply {
                        requestMethod = "GET"
                        connectTimeout = 30_000
                        readTimeout = 30_000
                        instanceFollowRedirects = true
                        doInput = true
                        connect()
                    }

                val responseCode = connection.responseCode

                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw IllegalStateException(
                        "Model download failed. HTTP error: $responseCode"
                    )
                }

                val totalBytes = connection.contentLengthLong

                if (totalBytes <= 0L) {
                    throw IllegalStateException(
                        "The model server did not provide a valid file size."
                    )
                }

                var downloadedBytes = 0L

                connection.inputStream.use { input ->
                    temporaryFile.outputStream().buffered().use { output ->
                        val buffer = ByteArray(16 * 1024)

                        while (true) {
                            networkAccessPolicy.ensureNetworkAllowed()

                            val bytesRead = input.read(buffer)

                            if (bytesRead == -1) {
                                break
                            }

                            output.write(buffer, 0, bytesRead)
                            downloadedBytes += bytesRead

                            _progress.value = ModelDownloadProgress(
                                downloadedBytes = downloadedBytes,
                                totalBytes = totalBytes,
                                isDownloading = true
                            )
                        }

                        output.flush()
                    }
                }

                if (downloadedBytes != totalBytes) {
                    throw IllegalStateException(
                        "Download incomplete. Please try again."
                    )
                }

                if (modelFile.exists()) {
                    modelFile.delete()
                }

                if (!temporaryFile.renameTo(modelFile)) {
                    temporaryFile.copyTo(modelFile, overwrite = true)
                    temporaryFile.delete()
                }

                _progress.value = ModelDownloadProgress(
                    downloadedBytes = modelFile.length(),
                    totalBytes = modelFile.length(),
                    isComplete = true
                )

            } finally {
                connection?.disconnect()
            }

        } catch (exception: Exception) {
            temporaryFile.delete()

            val message = exception.message
                ?: "Model download failed."

            _progress.value = ModelDownloadProgress(
                error = message
            )

            throw exception
        }
    }

    suspend fun deleteModel(): Boolean = withContext(Dispatchers.IO) {
        temporaryFile.delete()

        val deleted = if (modelFile.exists()) {
            modelFile.delete()
        } else {
            true
        }

        if (deleted) {
            _progress.value = ModelDownloadProgress()
        }

        deleted
    }
}

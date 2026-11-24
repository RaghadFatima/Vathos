
package com.vathos.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vathos.app.VathosApplication
import com.vathos.app.data.VathosPreferences
import com.vathos.app.data.ai.EmbeddingModelDownloader
import com.vathos.app.data.ai.QwenLocalModelManager
import com.vathos.app.data.ai.QwenModelDownloader
import kotlinx.coroutines.launch

private val ModelGreen = Color(0xFF10B981)

@Composable
fun ModelManagerScreen() {

    val context = androidx.compose.ui.platform.LocalContext.current
    val application = context.applicationContext as VathosApplication
    val container = application.container

    // Existing Qwen3 model downloader.
    val downloader = remember {
        QwenModelDownloader(application)
    }

    // New EmbeddingGemma model downloader.
    val embeddingDownloader = remember {
        EmbeddingModelDownloader(
            application,
            container.settingsRepository
        )
    }

    val modelManager =
        container.localModelManager as? QwenLocalModelManager

    val preferences by container.settingsRepository.preferences
        .collectAsState(initial = VathosPreferences())

    val progress by downloader.progress.collectAsState()
    val embeddingState by embeddingDownloader.state.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    var actionError by remember {
        mutableStateOf<String?>(null)
    }

    var embeddingError by remember {
        mutableStateOf<String?>(null)
    }

    val colorScheme = MaterialTheme.colorScheme
    val isLight = colorScheme.background == Color(0xFFF7F8FA)

    // Qwen3 status.
    val isDownloaded = downloader.isModelDownloaded()
    val isDownloading = progress.isDownloading

    val modelSizeMb = if (isDownloaded) {
        downloader.getModelFile().length() / (1024.0 * 1024.0)
    } else {
        0.0
    }

    // EmbeddingGemma status.
    val embeddingDownloaded = embeddingState.isDownloaded
    val embeddingDownloading = embeddingState.isDownloading

    val embeddingSizeMb =
        embeddingDownloader.getModelFile()?.length()?.div(
            1024.0 * 1024.0
        ) ?: 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 22.dp)
    ) {

        Text(
            "AI CONFIGURATION",
            color = ModelGreen,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )

        Spacer(Modifier.height(7.dp))

        Text(
            "Model Manager",
            color = colorScheme.onBackground,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(7.dp))

        Text(
            "Manage the private AI models stored on your device.",
            color = colorScheme.onSurfaceVariant,
            fontSize = 13.sp
        )

        Spacer(Modifier.height(23.dp))

        // MODEL STORAGE INFORMATION
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF102B20)
            )
        ) {
            Column(Modifier.padding(19.dp)) {

                Row(verticalAlignment = Alignment.CenterVertically) {

                    Box(
                        modifier = Modifier
                            .size(43.dp)
                            .background(
                                ModelGreen.copy(alpha = 0.16f),
                                RoundedCornerShape(13.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Storage,
                            null,
                            tint = ModelGreen
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Column {
                        Text(
                            "MODEL STORAGE",
                            color = Color(0xFFA7C9B7),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            String.format(
                                "%.1f MB",
                                modelSizeMb + embeddingSizeMb
                            ),
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(15.dp))

                Text(
                    "Total storage used by downloaded AI model files.",
                    color = Color(0xFFB7D2C4),
                    fontSize = 11.sp
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    "Qwen3: ${
                        if (isDownloaded) {
                            String.format("%.1f MB", modelSizeMb)
                        } else {
                            "Not downloaded"
                        }
                    }",
                    color = Color(0xFFB7D2C4),
                    fontSize = 11.sp
                )

                Text(
                    "EmbeddingGemma: ${
                        if (embeddingDownloaded) {
                            String.format("%.1f MB", embeddingSizeMb)
                        } else {
                            "Not downloaded"
                        }
                    }",
                    color = Color(0xFFB7D2C4),
                    fontSize = 11.sp
                )
            }
        }

        Spacer(Modifier.height(25.dp))

        // ACTIVE MODEL
        Text(
            "Active Model",
            color = colorScheme.onBackground,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isLight) {
                    Color(0xFFECFDF5)
                } else {
                    Color(0xFF173A2D)
                }
            )
        ) {
            Row(
                modifier = Modifier.padding(17.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Memory,
                    null,
                    tint = if (isLight) {
                        Color(0xFF047857)
                    } else {
                        Color(0xFF6EE7B7)
                    },
                    modifier = Modifier.size(28.dp)
                )

                Spacer(Modifier.width(13.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        if (isDownloaded) {
                            "Qwen3-0.6B INT4"
                        } else {
                            "No chat model downloaded"
                        },
                        color = if (isLight) {
                            Color(0xFF065F46)
                        } else {
                            Color(0xFFD1FAE5)
                        },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        if (isDownloaded) {
                            "Ready for local inference"
                        } else {
                            "Download the model to enable private AI."
                        },
                        color = if (isLight) {
                            Color(0xFF047857)
                        } else {
                            Color(0xFFA7C9B7)
                        },
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(25.dp))

        Text(
            "Available Models",
            color = colorScheme.onBackground,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(5.dp))

        Text(
            "Download and manage your on-device AI models.",
            color = colorScheme.onSurfaceVariant,
            fontSize = 12.sp
        )

        Spacer(Modifier.height(14.dp))

        // QWEN3 MODEL CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = colorScheme.surface
            ),
            border = BorderStroke(
                1.dp,
                colorScheme.outline.copy(alpha = 0.25f)
            ),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column(Modifier.padding(16.dp)) {

                Row(verticalAlignment = Alignment.CenterVertically) {

                    Box(
                        modifier = Modifier
                            .size(43.dp)
                            .background(
                                ModelGreen.copy(alpha = 0.10f),
                                RoundedCornerShape(13.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Memory,
                            null,
                            tint = ModelGreen,
                            modifier = Modifier.size(23.dp)
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(Modifier.weight(1f)) {
                        Text(
                            "Qwen3-0.6B INT4",
                            color = colorScheme.onSurface,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            "Approximately 332 MB",
                            color = colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }

                    Text(
                        if (isDownloaded) "INSTALLED" else "CHAT MODEL",
                        modifier = Modifier
                            .background(
                                ModelGreen.copy(alpha = 0.12f),
                                RoundedCornerShape(20.dp)
                            )
                            .padding(
                                horizontal = 9.dp,
                                vertical = 6.dp
                            ),
                        color = ModelGreen,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(16.dp))

                HorizontalDivider(
                    color = colorScheme.outline.copy(alpha = 0.35f)
                )

                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ModelDetail("QUANTIZATION", "4-bit INT4")
                    ModelDetail("FORMAT", "LiteRT-LM")
                    ModelDetail("BACKEND", "CPU")
                }

                Spacer(Modifier.height(17.dp))

                if (!isDownloaded) {

                    Button(
                        onClick = {
                            actionError = null

                            if (preferences.offlineMode) {
                                actionError =
                                    "Turn OFF Offline Mode in Settings before downloading the model."
                            } else {
                                coroutineScope.launch {
                                    try {
                                        downloader.downloadModel()
                                    } catch (exception: Exception) {
                                        actionError =
                                            exception.message
                                                ?: "Model download failed."
                                    }
                                }
                            }
                        },
                        enabled = !isDownloading,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ModelGreen,
                            contentColor = Color.White
                        )
                    ) {
                        if (isDownloading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Default.CloudDownload,
                                null,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(Modifier.width(8.dp))

                        Text(
                            if (isDownloading) {
                                "Downloading ${progress.percentage}%"
                            } else {
                                "Download Qwen3 Model"
                            },
                            fontSize = 12.sp
                        )
                    }

                    if (preferences.offlineMode) {
                        Spacer(Modifier.height(9.dp))

                        Text(
                            "Offline Mode is ON. Turn it OFF in Settings to download the model.",
                            color = colorScheme.error,
                            fontSize = 11.sp
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    modelManager?.closeModel()
                                    val deleted = downloader.deleteModel()

                                    actionError = if (deleted) {
                                        null
                                    } else {
                                        "Could not delete the model file."
                                    }
                                } catch (exception: Exception) {
                                    actionError =
                                        exception.message
                                            ?: "Could not close or delete the model."
                                }
                            }
                        },
                        enabled = !isDownloading,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            null,
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(Modifier.width(8.dp))

                        Text("Delete Qwen3 Model", fontSize = 12.sp)
                    }
                }

                if (actionError != null) {
                    Spacer(Modifier.height(12.dp))

                    Text(
                        actionError!!,
                        color = colorScheme.error,
                        fontSize = 12.sp
                    )
                }

                if (progress.error != null && actionError == null) {
                    Spacer(Modifier.height(12.dp))

                    Text(
                        progress.error!!,
                        color = colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // EMBEDDINGGEMMA MODEL CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = colorScheme.surface
            ),
            border = BorderStroke(
                1.dp,
                colorScheme.outline.copy(alpha = 0.25f)
            ),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column(Modifier.padding(16.dp)) {

                Row(verticalAlignment = Alignment.CenterVertically) {

                    Box(
                        modifier = Modifier
                            .size(43.dp)
                            .background(
                                Color(0xFF6366F1).copy(alpha = 0.10f),
                                RoundedCornerShape(13.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Memory,
                            null,
                            tint = Color(0xFF818CF8),
                            modifier = Modifier.size(23.dp)
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(Modifier.weight(1f)) {
                        Text(
                            "EmbeddingGemma 300M",
                            color = colorScheme.onSurface,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            "On-device semantic PDF search",
                            color = colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }

                    Text(
                        if (embeddingDownloaded) "INSTALLED" else "PDF SEARCH",
                        modifier = Modifier
                            .background(
                                Color(0xFF6366F1).copy(alpha = 0.12f),
                                RoundedCornerShape(20.dp)
                            )
                            .padding(
                                horizontal = 9.dp,
                                vertical = 6.dp
                            ),
                        color = Color(0xFF818CF8),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(16.dp))

                HorizontalDivider(
                    color = colorScheme.outline.copy(alpha = 0.35f)
                )

                Spacer(Modifier.height(14.dp))

                Text(
                    "This model converts document passages and questions into numerical embeddings. Vathos will use them to find relevant PDF content by meaning, not just matching keywords.",
                    color = colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    lineHeight = 17.sp
                )

                Spacer(Modifier.height(14.dp))

                Text(
                    when {
                        embeddingDownloaded ->
                            String.format(
                                "Stored in private app storage • %.1f MB",
                                embeddingSizeMb
                            )

                        embeddingDownloading ->
                            "Downloading EmbeddingGemma..."

                        else ->
                            "A separate model download is required for semantic PDF search."
                    },
                    color = colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )

                Spacer(Modifier.height(10.dp))

                if (embeddingDownloading) {
                    if (embeddingState.totalBytes > 0L) {
                        LinearProgressIndicator(
                            progress = {
                                embeddingState.progress / 100f
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(7.dp),
                            color = Color(0xFF818CF8),
                            trackColor = colorScheme.surfaceVariant,
                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    } else {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFF818CF8)
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    val downloadedMb =
                        embeddingState.downloadedBytes /
                                (1024.0 * 1024.0)

                    val totalMb =
                        embeddingState.totalBytes /
                                (1024.0 * 1024.0)

                    Text(
                        if (embeddingState.totalBytes > 0L) {
                            String.format(
                                "%.1f MB / %.1f MB • %d%%",
                                downloadedMb,
                                totalMb,
                                embeddingState.progress
                            )
                        } else {
                            String.format(
                                "%.1f MB downloaded",
                                downloadedMb
                            )
                        },
                        color = colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }

                Spacer(Modifier.height(15.dp))

                if (!embeddingDownloaded) {

                    Button(
                        onClick = {
                            embeddingError = null

                            if (preferences.offlineMode) {
                                embeddingError =
                                    "Turn OFF Offline Mode in Settings before downloading EmbeddingGemma."
                            } else {
                                coroutineScope.launch {
                                    try {
                                        embeddingDownloader.download()
                                    } catch (exception: Exception) {
                                        embeddingError =
                                            exception.message
                                                ?: "EmbeddingGemma download failed."
                                    }
                                }
                            }
                        },
                        enabled = !embeddingDownloading,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6366F1),
                            contentColor = Color.White
                        )
                    ) {
                        if (embeddingDownloading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Default.CloudDownload,
                                null,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(Modifier.width(8.dp))

                        Text(
                            if (embeddingDownloading) {
                                "Downloading ${embeddingState.progress}%"
                            } else {
                                "Download EmbeddingGemma"
                            },
                            fontSize = 12.sp
                        )
                    }

                    if (preferences.offlineMode) {
                        Spacer(Modifier.height(9.dp))

                        Text(
                            "Offline Mode is ON. Turn it OFF in Settings to download this model.",
                            color = colorScheme.error,
                            fontSize = 11.sp
                        )
                    }
                } else {

                    Text(
                        "100% • Downloaded",
                        color = ModelGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    val deleted =
                                        embeddingDownloader.deleteModel()

                                    embeddingError = if (deleted) {
                                        null
                                    } else {
                                        "Could not delete the EmbeddingGemma model file."
                                    }
                                } catch (exception: Exception) {
                                    embeddingError =
                                        exception.message
                                            ?: "Could not delete EmbeddingGemma."
                                }
                            }
                        },
                        enabled = !embeddingDownloading,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            null,
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(Modifier.width(8.dp))

                        Text(
                            "Delete EmbeddingGemma",
                            fontSize = 12.sp
                        )
                    }
                }

                if (embeddingError != null) {
                    Spacer(Modifier.height(12.dp))

                    Text(
                        embeddingError!!,
                        color = colorScheme.error,
                        fontSize = 12.sp
                    )
                }

                if (
                    embeddingState.error != null &&
                    embeddingError == null
                ) {
                    Spacer(Modifier.height(12.dp))

                    Text(
                        embeddingState.error!!,
                        color = colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(25.dp))

        // PRIVACY INFORMATION
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = colorScheme.surface
            )
        ) {
            Row(
                modifier = Modifier.padding(15.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Default.Memory,
                    null,
                    tint = ModelGreen
                )

                Spacer(Modifier.width(10.dp))

                Column {
                    Text(
                        "Private on-device AI",
                        color = colorScheme.onSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(Modifier.height(5.dp))

                    Text(
                        "Both models are stored in Vathos private internal storage. Qwen3 handles local chat and generation. EmbeddingGemma supports local semantic PDF retrieval. After both models are downloaded, these model operations can run without an internet connection.",
                        color = colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(25.dp))
    }
}

@Composable
private fun ModelDetail(
    label: String,
    value: String
) {
    val colors = MaterialTheme.colorScheme

    Column {
        Text(
            label,
            color = colors.onSurfaceVariant,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(5.dp))

        Text(
            value,
            color = colors.onSurface,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

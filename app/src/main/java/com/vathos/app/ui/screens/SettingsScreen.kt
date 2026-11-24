
package com.vathos.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vathos.app.VathosApplication
import com.vathos.app.data.AppTheme
import com.vathos.app.ui.viewmodel.NetworkUsageState
import com.vathos.app.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val SettingsGreen = Color(0xFF10B981)
private val SettingsMuted = Color(0xFF858B98)
private val SettingsBorder = Color(0xFFE9EBEF)

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val application = context.applicationContext as VathosApplication

    val settingsViewModel: SettingsViewModel = viewModel(
        factory = application.container.settingsViewModelFactory
    )

    val preferences by settingsViewModel.preferences.collectAsState()
    val networkUsage by settingsViewModel.networkUsage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showClearChatsDialog by remember { mutableStateOf(false) }
    var showClearDocumentsDialog by remember { mutableStateOf(false) }
    var pendingExportJson by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(settingsViewModel) {
        while (true) {
            settingsViewModel.refreshNetworkUsage()
            delay(1000)
        }
    }

    fun showMessage(message: String) {
        scope.launch {
            snackbarHostState.showSnackbar(message)
        }
    }

    val exportFileLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CreateDocument(
                "application/json"
            )
        ) { uri ->

            val json = pendingExportJson
            pendingExportJson = null

            if (uri == null || json == null) {
                showMessage("Data export cancelled.")
            } else {
                scope.launch {
                    val result = withContext(Dispatchers.IO) {
                        runCatching {
                            val outputStream =
                                context.contentResolver.openOutputStream(uri)
                                    ?: throw IllegalStateException(
                                        "Could not open the selected file."
                                    )

                            outputStream.bufferedWriter(Charsets.UTF_8).use {
                                it.write(json)
                            }
                        }
                    }

                    showMessage(
                        if (result.isSuccess) {
                            "Vathos data exported successfully."
                        } else {
                            "Export failed: ${
                                result.exceptionOrNull()?.message
                                    ?: "Unknown error"
                            }"
                        }
                    )
                }
            }
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(26.dp))

            Text(
                text = "Settings",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Manage your preferences and privacy.",
                fontSize = 14.sp,
                color = SettingsMuted
            )

            Spacer(modifier = Modifier.height(26.dp))

            SettingsSectionTitle("PRIVACY & SECURITY")

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, SettingsBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SettingsIcon(Icons.Outlined.Shield, SettingsGreen)

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Privacy-first design",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Built around on-device AI processing",
                                fontSize = 12.sp,
                                color = SettingsMuted
                            )
                        }

                        Surface(
                            color = SettingsGreen.copy(alpha = 0.10f),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                "DESIGNED",
                                modifier = Modifier.padding(
                                    horizontal = 9.dp,
                                    vertical = 5.dp
                                ),
                                color = SettingsGreen,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = SettingsBorder
                    )

                    SettingsSwitchRow(
                        icon = Icons.Outlined.WifiOff,
                        title = "Offline Mode",
                        subtitle = "Blocks Vathos model download requests",
                        checked = preferences.offlineMode,
                        onCheckedChange = {
                            settingsViewModel.setOfflineMode(it)
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "When enabled, Vathos blocks its Qwen3 and EmbeddingGemma model download requests. Chat inference and document processing run locally.",
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        color = SettingsMuted
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = SettingsBorder
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SettingsIcon(
                            Icons.Outlined.NetworkCheck,
                            SettingsGreen
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Network Traffic",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Android-reported app traffic",
                                fontSize = 11.sp,
                                color = SettingsMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (networkUsage.isSupported) {
                        NetworkUsageRow(
                            title = "Data sent by Vathos",
                            value = formatNetworkBytes(
                                networkUsage.sentBytes
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        NetworkUsageRow(
                            title = "Data received by Vathos",
                            value = formatNetworkBytes(
                                networkUsage.receivedBytes
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Measurements show the change in Android's app-UID counters since this meter started. Android's underlying counters are cumulative since device boot.",
                            fontSize = 10.sp,
                            lineHeight = 15.sp,
                            color = SettingsMuted
                        )
                    } else {
                        Text(
                            text = "Network traffic measurements are unavailable on this device.",
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = SettingsMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "These are app-level network counters, not server-specific measurements. They do not independently prove that no other Android network path exists.",
                        fontSize = 10.sp,
                        lineHeight = 15.sp,
                        color = SettingsMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            SettingsSectionTitle("APPEARANCE")

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, SettingsBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SettingsIcon(Icons.Outlined.Palette, SettingsGreen)

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                "App Theme",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Choose your preferred appearance",
                                fontSize = 12.sp,
                                color = SettingsMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            AppTheme.LIGHT to "Light",
                            AppTheme.DARK to "Dark",
                            AppTheme.SYSTEM to "System"
                        ).forEach { (theme, label) ->
                            val selected = preferences.theme == theme

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        settingsViewModel.setTheme(theme)
                                    },
                                shape = RoundedCornerShape(12.dp),
                                color = if (selected) {
                                    SettingsGreen.copy(alpha = 0.10f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                },
                                border = BorderStroke(
                                    1.dp,
                                    if (selected) SettingsGreen else SettingsBorder
                                )
                            ) {
                                Text(
                                    text = label,
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    textAlign = TextAlign.Center,
                                    fontSize = 13.sp,
                                    fontWeight = if (selected) {
                                        FontWeight.SemiBold
                                    } else {
                                        FontWeight.Medium
                                    },
                                    color = if (selected) {
                                        SettingsGreen
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            SettingsSectionTitle("DATA MANAGEMENT")

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, SettingsBorder)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    SettingsActionRow(
                        icon = Icons.Outlined.ChatBubbleOutline,
                        title = "Clear All Chats",
                        subtitle = "Remove your conversation history",
                        onClick = {
                            showClearChatsDialog = true
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        color = SettingsBorder
                    )

                    SettingsActionRow(
                        icon = Icons.Outlined.FolderOpen,
                        title = "Clear Documents",
                        subtitle = "Remove all stored PDF documents",
                        onClick = {
                            showClearDocumentsDialog = true
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        color = SettingsBorder
                    )

                    SettingsActionRow(
                        icon = Icons.Outlined.FileUpload,
                        title = "Export Data",
                        subtitle = "Save your app data as a JSON backup",
                        onClick = {
                            settingsViewModel.exportData { json, error ->
                                if (error != null) {
                                    showMessage(error)
                                } else if (json != null) {
                                    pendingExportJson = json
                                    exportFileLauncher.launch(
                                        "vathos_backup.json"
                                    )
                                } else {
                                    showMessage("Could not prepare your backup.")
                                }
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            SettingsSectionTitle("ABOUT")

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, SettingsBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .background(
                                    SettingsGreen.copy(alpha = 0.12f),
                                    RoundedCornerShape(14.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "V",
                                fontSize = 25.sp,
                                fontWeight = FontWeight.Bold,
                                color = SettingsGreen
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "VATHOS",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Private On-Device AI for Business",
                                fontSize = 11.sp,
                                color = SettingsMuted
                            )
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "v1.0.0",
                                modifier = Modifier.padding(
                                    horizontal = 9.dp,
                                    vertical = 6.dp
                                ),
                                fontSize = 11.sp,
                                color = SettingsMuted,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalDivider(color = SettingsBorder)

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        "Designed for private, local AI assistance.",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        "A privacy-focused AI experience designed to bring intelligent document assistance directly to your device.",
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = SettingsMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "MADE FOR PRIVATE, LOCAL AI",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 10.sp,
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = SettingsMuted
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }

    if (showClearChatsDialog) {
        AlertDialog(
            onDismissRequest = { showClearChatsDialog = false },
            title = { Text("Clear all chats?") },
            text = {
                Text("This will permanently delete your saved conversation history.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearChatsDialog = false
                        settingsViewModel.clearAllChats { error ->
                            showMessage(
                                error ?: "All chats have been deleted."
                            )
                        }
                    }
                ) {
                    Text("Clear chats", color = Color(0xFFD64545))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearChatsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showClearDocumentsDialog) {
        AlertDialog(
            onDismissRequest = { showClearDocumentsDialog = false },
            title = { Text("Clear all documents?") },
            text = {
                Text("This will permanently delete all saved PDF records and their stored files.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearDocumentsDialog = false
                        settingsViewModel.clearAllDocuments { error ->
                            showMessage(
                                error ?: "All documents have been deleted."
                            )
                        }
                    }
                ) {
                    Text("Clear documents", color = Color(0xFFD64545))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDocumentsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun NetworkUsageRow(
    title: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            fontSize = 12.sp,
            color = SettingsMuted
        )

        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun formatNetworkBytes(bytes: Long?): String {
    if (bytes == null) return "Unavailable"

    return when {
        bytes < 1024L -> "$bytes B"
        bytes < 1024L * 1024L ->
            String.format("%.2f KB", bytes / 1024.0)
        bytes < 1024L * 1024L * 1024L ->
            String.format("%.2f MB", bytes / (1024.0 * 1024.0))
        else ->
            String.format(
                "%.2f GB",
                bytes / (1024.0 * 1024.0 * 1024.0)
            )
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp,
        color = SettingsMuted
    )
}

@Composable
private fun SettingsIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(
                tint.copy(alpha = 0.10f),
                RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsIcon(icon, SettingsGreen)

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                subtitle,
                fontSize = 11.sp,
                color = SettingsMuted
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = SettingsGreen,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color.LightGray
            )
        )
    }
}

@Composable
private fun SettingsActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsIcon(icon, SettingsMuted)

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                subtitle,
                fontSize = 11.sp,
                color = SettingsMuted
            )
        }

        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = SettingsMuted,
            modifier = Modifier.size(20.dp)
        )
    }
}

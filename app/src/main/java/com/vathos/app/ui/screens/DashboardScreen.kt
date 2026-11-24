
package com.vathos.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vathos.app.VathosApplication
import com.vathos.app.domain.model.Document
import com.vathos.app.domain.model.Note
import com.vathos.app.domain.model.Task
import com.vathos.app.ui.viewmodel.HomeViewModel
import java.util.Locale

private val DashboardGreen = Color(0xFF10B981)

@Composable
fun DashboardScreen(
    onNavigateToVault: () -> Unit,
    onNavigateToChat: () -> Unit
) {
    val context = LocalContext.current
    val application =
        context.applicationContext as VathosApplication

    val homeViewModel: HomeViewModel = viewModel(
        factory = application.container.homeViewModelFactory
    )

    val documents by homeViewModel.documents.collectAsState()
    val notes by homeViewModel.notes.collectAsState()
    val tasks by homeViewModel.tasks.collectAsState()
    val conversationCount by homeViewModel.conversationCount.collectAsState()

    HomeDashboard(
        documents = documents,
        notes = notes,
        tasks = tasks,
        conversationCount = conversationCount,
        onUpload = onNavigateToVault,
        onAskAI = onNavigateToChat,
        onSummarize = onNavigateToVault
    )
}

@Composable
private fun HomeDashboard(
    documents: List<Document>,
    notes: List<Note>,
    tasks: List<Task>,
    conversationCount: Int,
    onUpload: () -> Unit,
    onAskAI: () -> Unit,
    onSummarize: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    // This measures imported PDF files only, not total app storage.
    val totalPdfStorage = documents.sumOf { it.size }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            DashboardGreen.copy(alpha = 0.12f),
                            RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "V",
                        color = DashboardGreen,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(11.dp))

                Column {
                    Text(
                        "VATHOS",
                        color = colors.onBackground,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )

                    Text(
                        "PRIVATE BUSINESS AI",
                        color = colors.onSurfaceVariant,
                        fontSize = 9.sp,
                        letterSpacing = 1.sp
                    )
                }
            }

            Row(
                modifier = Modifier
                    .background(
                        DashboardGreen.copy(alpha = 0.12f),
                        CircleShape
                    )
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .background(DashboardGreen, CircleShape)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    "NETWORK DISABLED",
                    color = DashboardGreen,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            "Good to see you!",
            color = colors.onSurfaceVariant,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            "Your Business Hub",
            color = colors.onBackground,
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(7.dp))

        Text(
            "Your documents and AI workspace, all in one place.",
            color = colors.onSurfaceVariant,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(25.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Overview",
                color = colors.onBackground,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                "YOUR WORKSPACE",
                color = colors.onSurfaceVariant,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(13.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                title = "Documents",
                value = documents.size.toString(),
                icon = Icons.Default.Description,
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = "Conversations",
                value = conversationCount.toString(),
                icon = Icons.Default.Chat,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                title = "PDF Storage",
                value = formatStorage(totalPdfStorage),
                icon = Icons.Default.Storage,
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = "Active Model",
                value = "Demo Mode",
                icon = Icons.Default.Memory,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF102B20)
            )
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(45.dp)
                        .background(
                            DashboardGreen.copy(alpha = 0.16f),
                            RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CloudOff,
                        contentDescription = null,
                        tint = DashboardGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(13.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Privacy comes first",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        "This app currently has no INTERNET permission. A real on-device AI model is not connected yet.",
                        color = Color(0xFFB7D2C4),
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(25.dp))

        Text(
            "Quick Actions",
            color = colors.onBackground,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            "Get things done faster",
            color = colors.onSurfaceVariant,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(13.dp))

        ActionCard(
            title = "Upload Document",
            description = "Add business files to your document vault",
            icon = Icons.Default.UploadFile,
            onClick = onUpload
        )

        Spacer(modifier = Modifier.height(11.dp))

        ActionCard(
            title = "Ask AI",
            description = "Start a conversation with your AI assistant",
            icon = Icons.Default.Chat,
            onClick = onAskAI
        )

        Spacer(modifier = Modifier.height(11.dp))

        ActionCard(
            title = "Summarize Documents",
            description = "Explore your business documents",
            icon = Icons.Default.AutoAwesome,
            onClick = onSummarize
        )

        Spacer(modifier = Modifier.height(25.dp))

        Text(
            "Recent Notes",
            color = colors.onBackground,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (notes.isEmpty()) {
            SimplePreviewCard(
                title = "No notes yet",
                subtitle = "Create a note through your AI chat"
            )
        } else {
            notes.take(3).forEach { note ->
                SimplePreviewCard(
                    title = note.title,
                    subtitle = note.content
                )

                Spacer(modifier = Modifier.height(9.dp))
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            "Tasks",
            color = colors.onBackground,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (tasks.isEmpty()) {
            SimplePreviewCard(
                title = "No tasks yet",
                subtitle = "Create a task through your AI chat"
            )
        } else {
            tasks.take(3).forEach { task ->
                SimplePreviewCard(
                    title = task.title,
                    subtitle = "Due: ${task.dueDate}"
                )

                Spacer(modifier = Modifier.height(9.dp))
            }
        }

        Spacer(modifier = Modifier.height(25.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Recent Documents",
                color = colors.onBackground,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            TextButton(onClick = onUpload) {
                Text(
                    "View Vault",
                    color = DashboardGreen,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(5.dp))

        if (documents.isEmpty()) {
            DocumentPreviewCard(
                name = "No documents yet",
                details = "Upload a PDF to get started"
            )
        } else {
            documents.take(2).forEach { document ->
                DocumentPreviewCard(
                    name = document.name,
                    details = "${document.pages} pages  •  PDF Document"
                )

                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun SimplePreviewCard(
    title: String,
    subtitle: String
) {
    val colors = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                title,
                color = colors.onSurface,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                subtitle,
                color = colors.onSurfaceVariant,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                maxLines = 3
            )
        }
    }
}

private fun formatStorage(bytes: Long): String {
    val megabytes = bytes / (1024.0 * 1024.0)

    return if (megabytes >= 1024) {
        String.format(
            Locale.US,
            "%.1f GB",
            megabytes / 1024.0
        )
    } else {
        String.format(
            Locale.US,
            "%.1f MB",
            megabytes
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(modifier = Modifier.padding(15.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        DashboardGreen.copy(alpha = 0.10f),
                        RoundedCornerShape(11.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = DashboardGreen,
                    modifier = Modifier.size(19.dp)
                )
            }

            Spacer(modifier = Modifier.height(13.dp))

            Text(
                title,
                color = colors.onSurfaceVariant,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                value,
                color = colors.onSurface,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ActionCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(17.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(45.dp)
                    .background(
                        DashboardGreen.copy(alpha = 0.10f),
                        RoundedCornerShape(13.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = DashboardGreen,
                    modifier = Modifier.size(23.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    color = colors.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    description,
                    color = colors.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }

            Icon(
                Icons.Default.ArrowForward,
                contentDescription = null,
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun DocumentPreviewCard(
    name: String,
    details: String
) {
    val colors = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        Color(0xFFDC5555).copy(alpha = 0.10f),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Description,
                    contentDescription = null,
                    tint = Color(0xFFDC5555),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    name,
                    color = colors.onSurface,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    details,
                    color = colors.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }

            Icon(
                Icons.Default.ArrowForward,
                contentDescription = null,
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(17.dp)
            )
        }
    }
}

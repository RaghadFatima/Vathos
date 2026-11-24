
package com.vathos.app.ui.screens

import android.text.format.DateFormat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vathos.app.VathosApplication
import com.vathos.app.domain.model.Document
import com.vathos.app.ui.viewmodel.DocumentVaultViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val VaultGreen = Color(0xFF10B981)

@Composable
fun DocumentVaultScreen() {

    val context = LocalContext.current
    val application = context.applicationContext as VathosApplication

    val vaultViewModel: DocumentVaultViewModel = viewModel(
        factory = application.container.documentVaultViewModelFactory
    )

    val documents by vaultViewModel.documents.collectAsState()
    val searchQuery by vaultViewModel.searchQuery.collectAsState()
    val isLoading by vaultViewModel.isLoading.collectAsState()
    val error by vaultViewModel.error.collectAsState()

    var selectedFilter by remember { mutableStateOf("All") }
    var pendingDelete by remember { mutableStateOf<Document?>(null) }

    val colorScheme = MaterialTheme.colorScheme
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val documentPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            vaultViewModel.importPdf(it.toString())
        }
    }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            vaultViewModel.clearError()
        }
    }

    val recentCutoff = System.currentTimeMillis() -
            (7L * 24 * 60 * 60 * 1000)

    val filteredDocuments = documents.filter { document ->
        when (selectedFilter) {
            "Recent" -> document.date >= recentCutoff
            "PDF" -> document.name.endsWith(".pdf", ignoreCase = true)
            else -> true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(22.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "DOCUMENT MANAGEMENT",
                        color = VaultGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )

                    Spacer(modifier = Modifier.height(7.dp))

                    Text(
                        "Document Vault",
                        color = colorScheme.onBackground,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .size(43.dp)
                        .background(
                            colorScheme.surface,
                            RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = "Filter documents",
                        tint = colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Your business files, organized in one private workspace.",
                color = colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Document count card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF102B20)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "YOUR LIBRARY",
                            color = Color(0xFFA7C9B7),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            "${documents.size} Documents",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            "Available in your workspace",
                            color = Color(0xFFB7D2C4),
                            fontSize = 11.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(
                                VaultGreen.copy(alpha = 0.17f),
                                RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Description,
                            contentDescription = null,
                            tint = VaultGreen,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Search
            OutlinedTextField(
                value = searchQuery,
                onValueChange = vaultViewModel::searchDocuments,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        "Search your documents",
                        fontSize = 13.sp,
                        color = colorScheme.onSurfaceVariant
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = colorScheme.onSurfaceVariant
                    )
                },
                shape = RoundedCornerShape(15.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VaultGreen,
                    unfocusedBorderColor = colorScheme.outline,
                    focusedContainerColor = colorScheme.surface,
                    unfocusedContainerColor = colorScheme.surface,
                    focusedTextColor = colorScheme.onSurface,
                    unfocusedTextColor = colorScheme.onSurface,
                    cursorColor = VaultGreen
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Filters
            Row(
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                listOf("All", "Recent", "PDF").forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = {
                            Text(filter, fontSize = 12.sp)
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (
                                colorScheme.background == Color(0xFFF7F8FA)
                            ) {
                                Color(0xFFD1FAE5)
                            } else {
                                Color(0xFF164534)
                            },
                            selectedLabelColor = if (
                                colorScheme.background == Color(0xFFF7F8FA)
                            ) {
                                Color(0xFF047857)
                            } else {
                                Color(0xFF6EE7B7)
                            },
                            labelColor = colorScheme.onSurfaceVariant
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedFilter == filter,
                            borderColor = colorScheme.outline,
                            selectedBorderColor = VaultGreen
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // List heading
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Your Documents",
                    color = colorScheme.onBackground,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "${filteredDocuments.size} files",
                    color = colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Document list
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 95.dp),
                verticalArrangement = Arrangement.spacedBy(11.dp)
            ) {
                if (filteredDocuments.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 45.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Description,
                                contentDescription = null,
                                tint = colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(42.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                "No documents found",
                                color = colorScheme.onBackground,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(5.dp))

                            Text(
                                "Upload a PDF or try another search.",
                                color = colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                items(
                    items = filteredDocuments,
                    key = { it.id }
                ) { document ->
                    VaultDocumentCard(
                        document = document,
                        onDelete = { pendingDelete = document },
                        onGenerateSummary = {
                            vaultViewModel.generateSummary(document.id)
                        }
                    )
                }
            }
        }

        // Upload button
        FloatingActionButton(
            onClick = {
                documentPicker.launch(arrayOf("application/pdf"))
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp),
            containerColor = VaultGreen,
            contentColor = Color.White,
            shape = RoundedCornerShape(17.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null
                )

                Spacer(modifier = Modifier.width(7.dp))

                Text(
                    "Upload",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(38.dp),
                color = VaultGreen
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 85.dp)
        )
    }

    // Delete confirmation
    pendingDelete?.let { document ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = {
                Text("Delete document?")
            },
            text = {
                Text("Are you sure you want to delete ${document.name}? This cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        vaultViewModel.deleteDocument(document.id)
                        pendingDelete = null
                    }
                ) {
                    Text(
                        "Delete",
                        color = Color(0xFFDC5555)
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { pendingDelete = null }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun VaultDocumentCard(
    document: Document,
    onDelete: () -> Unit,
    onGenerateSummary: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(17.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(15.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(45.dp)
                        .background(
                            Color(0xFFFEF2F2),
                            RoundedCornerShape(13.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.PictureAsPdf,
                        contentDescription = "PDF document",
                        tint = Color(0xFFDC5555),
                        modifier = Modifier.size(25.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        document.name,
                        color = colorScheme.onSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        "${document.pages} pages  •  ${formatFileSize(document.size)}",
                        color = colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }

                Box {
                    IconButton(
                        onClick = { menuExpanded = true }
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Generate summary") },
                            onClick = {
                                menuExpanded = false
                                onGenerateSummary()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Delete document") },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(13.dp))

            HorizontalDivider(
                color = colorScheme.outline.copy(alpha = 0.35f)
            )

            Spacer(modifier = Modifier.height(11.dp))

            Text(
                "SUMMARY PREVIEW",
                color = VaultGreen,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                document.summary.ifBlank {
                    "No summary generated yet. Use the menu to generate one."
                },
                color = colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(13.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    "Added ${formatDate(document.date)}",
                    color = colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    "PDF",
                    color = Color(0xFF047857),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(
                            Color(0xFFECFDF5),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(
                            horizontal = 9.dp,
                            vertical = 5.dp
                        )
                )
            }
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    val megabytes = bytes / (1024.0 * 1024.0)

    return if (megabytes >= 1024) {
        String.format(Locale.getDefault(), "%.1f GB", megabytes / 1024.0)
    } else {
        String.format(Locale.getDefault(), "%.1f MB", megabytes)
    }
}

private fun formatDate(timestamp: Long): String {
    return SimpleDateFormat(
        "MMM dd, yyyy",
        Locale.getDefault()
    ).format(Date(timestamp))
}

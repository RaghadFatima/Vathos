
package com.vathos.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.KeyboardVoice
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vathos.app.VathosApplication
import com.vathos.app.data.ai.QwenLocalModelManager
import com.vathos.app.domain.model.ChatMessage
import com.vathos.app.ui.viewmodel.ChatViewModel
import com.vathos.app.ui.viewmodel.DocumentVaultViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val ChatGreen = Color(0xFF10B981)

@Composable
fun ChatScreen(
    onNavigateToModelManager: () -> Unit = {}
) {

    val context = LocalContext.current
    val application = context.applicationContext as VathosApplication
    val lifecycleOwner = LocalLifecycleOwner.current

    val chatViewModel: ChatViewModel = viewModel(
        factory = application.container.chatViewModelFactory
    )

    val documentViewModel: DocumentVaultViewModel = viewModel(
        factory = application.container.documentVaultViewModelFactory
    )

    val qwenManager =
        application.container.localModelManager as? QwenLocalModelManager

    val speedFlow: StateFlow<Int> = remember(qwenManager) {
        qwenManager?.tokensPerSecond ?: MutableStateFlow(0)
    }

    val tokensPerSecond by speedFlow.collectAsState()

    val messages by chatViewModel.messages.collectAsState()
    val isTyping by chatViewModel.isTyping.collectAsState()
    val streamingResponse by chatViewModel.streamingResponse.collectAsState()
    val error by chatViewModel.error.collectAsState()
    val documents by documentViewModel.documents.collectAsState()
    val selectedDocumentId by chatViewModel.selectedDocumentId.collectAsState()
    val isModelAvailable by chatViewModel.isModelAvailable.collectAsState()

    val selectedDocument = documents.firstOrNull {
        it.id == selectedDocumentId
    }

    var messageText by remember { mutableStateOf("") }
    var documentMenuExpanded by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val colors = MaterialTheme.colorScheme

    // Check model availability when Chat first opens.
    LaunchedEffect(Unit) {
        chatViewModel.refreshModelAvailability()
    }

    // Refresh availability when returning from Model Manager.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                chatViewModel.refreshModelAvailability()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            chatViewModel.clearError()
        }
    }

    LaunchedEffect(messages.size, isTyping, streamingResponse) {
        val lastIndex = messages.size + if (isTyping) 1 else 0

        if (lastIndex > 0) {
            listState.animateScrollToItem(lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {

        // Chat header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface)
                .padding(horizontal = 18.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        ChatGreen.copy(alpha = 0.12f),
                        RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.SmartToy,
                    contentDescription = null,
                    tint = ChatGreen,
                    modifier = Modifier.size(25.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {

                Text(
                    "Vathos AI",
                    color = colors.onSurface,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    "Your private business assistant",
                    color = colors.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }

            IconButton(
                onClick = {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            "More chat options will be available in a future update."
                        )
                    }
                }
            ) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = colors.onSurfaceVariant
                )
            }
        }

        HorizontalDivider(color = colors.outline.copy(alpha = 0.25f))

        // Private model status
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Row(
                modifier = Modifier
                    .background(Color(0xFFECFDF5), CircleShape)
                    .padding(horizontal = 11.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .background(
                            if (isModelAvailable) ChatGreen else Color.Gray,
                            CircleShape
                        )
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    if (isModelAvailable) "PRIVATE MODEL" else "MODEL REQUIRED",
                    color = if (isModelAvailable) {
                        Color(0xFF047857)
                    } else {
                        colors.onSurfaceVariant
                    },
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                if (isModelAvailable) "On-Device AI" else "Qwen3 not installed",
                color = colors.onSurfaceVariant,
                fontSize = 11.sp
            )
        }

        // Download prompt when the model is missing.
        if (!isModelAvailable) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 5.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colors.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.Start
                ) {

                    Icon(
                        Icons.Default.CloudDownload,
                        contentDescription = null,
                        tint = ChatGreen,
                        modifier = Modifier.size(30.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        "Download Model to Start Private Chat",
                        color = colors.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(7.dp))

                    Text(
                        "Qwen3-0.6B is required to generate responses on your device. Download it once to start using private, on-device AI.",
                        color = colors.onSurfaceVariant,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(15.dp))

                    Button(
                        onClick = onNavigateToModelManager,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ChatGreen,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            Icons.Default.CloudDownload,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            "Open Model Manager",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Conversation and live response
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            state = listState,
            contentPadding = PaddingValues(
                horizontal = 16.dp,
                vertical = 12.dp
            ),
            verticalArrangement = Arrangement.spacedBy(17.dp)
        ) {

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        "TODAY",
                        color = colors.onSurfaceVariant,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        "Ask questions about your business documents.",
                        color = colors.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }

            items(
                items = messages,
                key = { it.id }
            ) { message ->
                ChatMessageBubble(message)
            }

            if (isTyping) {
                item(key = "vathos_live_response") {
                    if (streamingResponse.isNotBlank()) {
                        StreamingMessageBubble(streamingResponse)
                    } else {
                        TypingIndicator()
                    }
                }
            }
        }

        // Selected document
        if (selectedDocument != null) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surfaceVariant.copy(alpha = 0.35f))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    Icons.Default.AttachFile,
                    contentDescription = null,
                    tint = ChatGreen,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(7.dp))

                Text(
                    text = selectedDocument.name,
                    color = colors.onSurface,
                    fontSize = 11.sp,
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )

                TextButton(
                    onClick = {
                        chatViewModel.selectDocument(null)
                    }
                ) {
                    Text("Remove", color = ChatGreen, fontSize = 11.sp)
                }
            }
        }

        // Live model performance information
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface)
                .padding(horizontal = 18.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = ChatGreen,
                modifier = Modifier.size(14.dp)
            )

            Spacer(modifier = Modifier.width(7.dp))

            Text(
                "Qwen3-0.6B",
                color = colors.onSurface,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                "Est. $tokensPerSecond tok/s • On-Device",
                color = colors.onSurfaceVariant,
                fontSize = 10.sp
            )
        }

        // Message composer
        if (isModelAvailable) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surface)
                    .padding(horizontal = 13.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            "Ask about your documents...",
                            fontSize = 12.sp,
                            color = colors.onSurfaceVariant
                        )
                    },
                    shape = RoundedCornerShape(17.dp),
                    singleLine = true,
                    enabled = !isTyping,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ChatGreen,
                        unfocusedBorderColor = colors.outline,
                        focusedTextColor = colors.onSurface,
                        unfocusedTextColor = colors.onSurface,
                        focusedContainerColor = colors.surfaceVariant.copy(alpha = 0.35f),
                        unfocusedContainerColor = colors.surfaceVariant.copy(alpha = 0.35f),
                        cursorColor = ChatGreen
                    ),
                    leadingIcon = {
                        Box {

                            IconButton(
                                onClick = {
                                    documentMenuExpanded = true
                                },
                                enabled = !isTyping
                            ) {
                                Icon(
                                    Icons.Default.AttachFile,
                                    contentDescription = "Attach document",
                                    tint = colors.onSurfaceVariant,
                                    modifier = Modifier.size(19.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = documentMenuExpanded,
                                onDismissRequest = {
                                    documentMenuExpanded = false
                                }
                            ) {

                                DropdownMenuItem(
                                    text = { Text("No document") },
                                    onClick = {
                                        chatViewModel.selectDocument(null)
                                        documentMenuExpanded = false
                                    }
                                )

                                if (documents.isEmpty()) {

                                    DropdownMenuItem(
                                        text = { Text("No PDFs in your Vault") },
                                        enabled = false,
                                        onClick = {}
                                    )

                                } else {

                                    documents.forEach { document ->

                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    document.name,
                                                    maxLines = 1
                                                )
                                            },
                                            onClick = {
                                                chatViewModel.selectDocument(document.id)
                                                documentMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        "Voice input will be implemented later."
                                    )
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.KeyboardVoice,
                                contentDescription = "Voice input",
                                tint = colors.onSurfaceVariant,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.width(9.dp))

                IconButton(
                    onClick = {
                        val typedMessage = messageText.trim()

                        if (typedMessage.isNotEmpty() && !isTyping) {
                            chatViewModel.sendMessage(typedMessage)
                            messageText = ""
                        }
                    },
                    enabled = messageText.isNotBlank() && !isTyping,
                    modifier = Modifier
                        .size(49.dp)
                        .background(
                            ChatGreen,
                            RoundedCornerShape(15.dp)
                        )
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Send message",
                        tint = Color.White,
                        modifier = Modifier.size(21.dp)
                    )
                }
            }
        }

        SnackbarHost(hostState = snackbarHostState)
    }
}

@Composable
private fun ChatMessageBubble(message: ChatMessage) {

    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) {
            Arrangement.End
        } else {
            Arrangement.Start
        },
        verticalAlignment = Alignment.Top
    ) {

        if (!message.isUser) {

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        ChatGreen.copy(alpha = 0.12f),
                        RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.SmartToy,
                    contentDescription = null,
                    tint = ChatGreen,
                    modifier = Modifier.size(19.dp)
                )
            }

            Spacer(modifier = Modifier.width(9.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = 290.dp),
            horizontalAlignment = if (message.isUser) {
                Alignment.End
            } else {
                Alignment.Start
            }
        ) {

            Card(
                shape = RoundedCornerShape(
                    topStart = 17.dp,
                    topEnd = 17.dp,
                    bottomStart = if (message.isUser) 17.dp else 5.dp,
                    bottomEnd = if (message.isUser) 5.dp else 17.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (message.isUser) {
                        Color(0xFF102B20)
                    } else {
                        colors.surface
                    }
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 1.dp
                )
            ) {

                Text(
                    text = message.message,
                    modifier = Modifier.padding(15.dp),
                    color = if (message.isUser) Color.White else colors.onSurface,
                    fontSize = 13.sp,
                    lineHeight = 21.sp
                )
            }

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                formatMessageTime(message.timestamp),
                color = colors.onSurfaceVariant,
                fontSize = 9.sp
            )
        }
    }
}

@Composable
private fun StreamingMessageBubble(text: String) {

    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {

        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    ChatGreen.copy(alpha = 0.12f),
                    RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.SmartToy,
                contentDescription = null,
                tint = ChatGreen,
                modifier = Modifier.size(19.dp)
            )
        }

        Spacer(modifier = Modifier.width(9.dp))

        Card(
            modifier = Modifier.widthIn(max = 290.dp),
            shape = RoundedCornerShape(
                topStart = 17.dp,
                topEnd = 17.dp,
                bottomStart = 5.dp,
                bottomEnd = 17.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = colors.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(15.dp),
                color = colors.onSurface,
                fontSize = 13.sp,
                lineHeight = 21.sp
            )
        }
    }
}

@Composable
private fun TypingIndicator() {

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    ChatGreen.copy(alpha = 0.12f),
                    RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.SmartToy,
                contentDescription = null,
                tint = ChatGreen,
                modifier = Modifier.size(19.dp)
            )
        }

        Spacer(modifier = Modifier.width(9.dp))

        Card(
            shape = RoundedCornerShape(15.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Text(
                "Vathos is thinking...",
                modifier = Modifier.padding(
                    horizontal = 15.dp,
                    vertical = 12.dp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }
    }
}

private fun formatMessageTime(timestamp: Long): String {
    return SimpleDateFormat(
        "h:mm a",
        Locale.getDefault()
    ).format(Date(timestamp))
}

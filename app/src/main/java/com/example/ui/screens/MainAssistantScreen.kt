package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.assistant.AssistantManager
import com.example.assistant.AssistantState
import com.example.assistant.ChatMessage
import com.example.assistant.MessageSender
import com.example.ui.components.ActionConfirmationDialog
import com.example.ui.components.MyraaAvatar
import com.example.ui.components.StatusPill
import com.example.ui.components.WaveformVisualizer
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberMagenta
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.ObsidianCardBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun MainAssistantScreen(
    assistantManager: AssistantManager,
    onNavigateToVision: () -> Unit,
    onNavigateToMemory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDeviceOptimization: () -> Unit,
    onNavigateToDeveloperCopilot: () -> Unit,
    onNavigateToPrivacy: () -> Unit
) {
    val state by assistantManager.assistantState.collectAsState()
    val messages by assistantManager.chatMessages.collectAsState()
    val statusText by assistantManager.currentStatusText.collectAsState()
    val isSpeaking by assistantManager.audioOutputManager.isSpeaking.collectAsState()
    val isListening by assistantManager.audioInputManager.isListening.collectAsState()
    val rmsDb by assistantManager.audioInputManager.rmsDb.collectAsState()
    val confirmationRequest by assistantManager.confirmationManager.currentRequest.collectAsState()

    var textInput by remember { mutableStateOf("") }
    var menuExpanded by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val scope = rememberCoroutineScope()

    // Handle safety confirmation dialog
    confirmationRequest?.let { req ->
        ActionConfirmationDialog(
            request = req,
            onConfirm = {
                scope.launch {
                    val result = assistantManager.confirmationManager.confirm()
                    if (result != null) {
                        assistantManager.audioOutputManager.speak(result.spokenResponse)
                    }
                }
            },
            onDismiss = {
                assistantManager.confirmationManager.cancel()
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF04060E),
                        ObsidianBackground,
                        Color(0xFF070B16)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .testTag("main_assistant_screen")
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MYRAA",
                        color = CyberCyan,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "Realme 12 Pro 5G Edition",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusPill(
                        text = when (state) {
                            AssistantState.LISTENING -> "LISTENING"
                            AssistantState.THINKING -> "THINKING"
                            AssistantState.SPEAKING -> "SPEAKING"
                            AssistantState.EXECUTING -> "EXECUTING"
                            AssistantState.OFFLINE -> "LOCAL ONLY"
                            else -> "READY"
                        },
                        dotColor = when (state) {
                            AssistantState.LISTENING -> CyberCyan
                            AssistantState.THINKING -> Color(0xFFFFB703)
                            AssistantState.SPEAKING -> CyberMagenta
                            AssistantState.ERROR -> Color(0xFFEF4444)
                            else -> Color(0xFF10B981)
                        }
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.testTag("menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = Color.White.copy(alpha = 0.8f)
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier.background(ObsidianSurfaceVariant)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Camera Vision", color = Color.White) },
                            onClick = { menuExpanded = false; onNavigateToVision() }
                        )
                        DropdownMenuItem(
                            text = { Text("Memory Vault", color = Color.White) },
                            onClick = { menuExpanded = false; onNavigateToMemory() }
                        )
                        DropdownMenuItem(
                            text = { Text("Developer Copilot", color = Color.White) },
                            onClick = { menuExpanded = false; onNavigateToDeveloperCopilot() }
                        )
                        DropdownMenuItem(
                            text = { Text("Realme Optimization", color = Color.White) },
                            onClick = { menuExpanded = false; onNavigateToDeviceOptimization() }
                        )
                        DropdownMenuItem(
                            text = { Text("Privacy & Security", color = Color.White) },
                            onClick = { menuExpanded = false; onNavigateToPrivacy() }
                        )
                        DropdownMenuItem(
                            text = { Text("Settings", color = Color.White) },
                            onClick = { menuExpanded = false; onNavigateToSettings() }
                        )
                        DropdownMenuItem(
                            text = { Text("Clear Chat", color = Color(0xFFEF4444)) },
                            onClick = { menuExpanded = false; assistantManager.clearHistory() }
                        )
                    }
                }
            }

            // Central Interactive Hologram Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    MyraaAvatar(
                        state = state,
                        onClick = {
                            if (isSpeaking) {
                                assistantManager.interruptSpeech()
                            } else if (isListening) {
                                assistantManager.stopListening()
                            } else {
                                assistantManager.startListening()
                            }
                        },
                        size = 130.dp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = statusText,
                        color = TextPrimary.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    WaveformVisualizer(
                        amplitude = if (isListening) (rmsDb / 10f) else if (isSpeaking) 0.85f else 0.05f,
                        isListeningOrSpeaking = isListening || isSpeaking,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }

            // Quick Shortcut Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                val chips = listOf(
                    "🔦 Flashlight on" to "Flashlight on karo",
                    "💬 WhatsApp" to "WhatsApp kholo",
                    "🔋 Battery status" to "Battery kitni hai?",
                    "⏱️ 5 min timer" to "5 minute ka timer lagao",
                    "⚙️ Wi-Fi Settings" to "Wi-Fi settings kholo",
                    "🧠 Kya yaad hai?" to "Myraa, tumhe mere baare mein kya yaad hai?"
                )
                items(chips) { (label, command) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(ObsidianSurface)
                            .border(1.dp, ObsidianCardBorder, RoundedCornerShape(16.dp))
                            .clickable {
                                assistantManager.handleUserInput(command)
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = label,
                            color = CyberCyan.copy(alpha = 0.9f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Conversation & Actions History Feed
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    ChatBubble(message = msg)
                }
            }

            // Bottom Input Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ObsidianSurface.copy(alpha = 0.95f))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Camera Vision shortcut button
                    IconButton(
                        onClick = onNavigateToVision,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(ObsidianSurfaceVariant)
                            .testTag("camera_vision_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Camera Vision",
                            tint = CyberCyan
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Text Input Field
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = {
                            Text(
                                text = "Bolo ya type karo Piyush...",
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = ObsidianCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = CyberCyan
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("assistant_text_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    if (textInput.isNotBlank()) {
                        // Send text button
                        IconButton(
                            onClick = {
                                val text = textInput
                                textInput = ""
                                assistantManager.handleUserInput(text)
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(CyberCyan)
                                .testTag("send_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = Color(0xFF020617)
                            )
                        }
                    } else {
                        // Voice Mic Button
                        val micBgColor = if (isListening) CyberMagenta else CyberCyan
                        IconButton(
                            onClick = {
                                if (isListening) {
                                    assistantManager.stopListening()
                                } else {
                                    assistantManager.startListening()
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(micBgColor)
                                .testTag("mic_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = if (isListening) "Stop Listening" else "Start Listening",
                                tint = if (isListening) Color.White else Color(0xFF020617)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val isUser = message.sender == MessageSender.USER
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val bgColor = if (isUser) Color(0xFF1E293B) else ObsidianSurfaceVariant
    val borderColor = if (isUser) Color(0xFF334155) else ObsidianCardBorder

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = bgColor),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            modifier = Modifier
                .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                .padding(horizontal = 4.dp)
                .testTag(if (isUser) "user_chat_bubble" else "myraa_chat_bubble")
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                if (!isUser) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "MYRAA",
                            color = CyberCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                }

                Text(
                    text = message.text,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                if (message.actionDetail != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF083344))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Action: ${message.actionDetail}",
                            color = CyberCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

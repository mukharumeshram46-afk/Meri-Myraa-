package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Code
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.assistant.AssistantManager
import com.example.developer.DevQueryResponse
import com.example.developer.DeveloperAssistant
import com.example.ui.components.StatusPill
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.ObsidianCardBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun DeveloperCopilotScreen(
    assistantManager: AssistantManager,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val devAssistant = remember { DeveloperAssistant(assistantManager.geminiRepository) }

    var devQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var devResponse by remember { mutableStateOf<DevQueryResponse?>(null) }

    val presetQueries = listOf(
        "Compose State Best Practices" to "Explain best practices for ViewModel StateFlow and remember in Jetpack Compose to avoid unnecessary recompositions.",
        "Coroutines Concurrency" to "Explain structured concurrency with CoroutineScope, viewModelScope, and Dispatchers.IO vs Main.",
        "Room KSP Migration" to "How to write safe Room database migrations with KSP in modern Android Kotlin?",
        "Clean Architecture MVVM" to "Explain MVVM with Clean Architecture layer boundaries for Android production apps."
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("developer_copilot_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("dev_back_button")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Developer Copilot",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Engineering & Android AI Assistant for Piyush",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
                StatusPill(text = "DEV COPILOT", dotColor = CyberCyan)
            }

            // Presets
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(presetQueries) { (title, query) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(ObsidianSurface)
                            .border(1.dp, ObsidianCardBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = title,
                            color = CyberCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.testTag("preset_$title")
                        )
                    }
                }
            }

            // Main response content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(ObsidianSurface)
                    .border(1.dp, ObsidianCardBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                if (isLoading) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = CyberCyan)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Analyzing query for Piyush...", color = TextSecondary, fontSize = 13.sp)
                    }
                } else if (devResponse != null) {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text(
                            text = devResponse?.title ?: "",
                            color = CyberCyan,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = devResponse?.explanation ?: "",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            tint = CyberCyan.copy(alpha = 0.6f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Ask MYRAA any technical Android, Kotlin, Architecture, or API question.",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            // Input Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ObsidianSurfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = devQuery,
                    onValueChange = { devQuery = it },
                    placeholder = { Text("Ask technical question...", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = CyberCyan
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        val q = devQuery
                        if (q.isNotBlank()) {
                            devQuery = ""
                            isLoading = true
                            scope.launch {
                                val res = devAssistant.consultDeveloper(q)
                                devResponse = res
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier.testTag("dev_submit_button")
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Submit", tint = CyberCyan)
                }
            }
        }
    }
}

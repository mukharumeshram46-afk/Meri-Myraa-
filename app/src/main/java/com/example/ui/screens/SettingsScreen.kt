package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.settings.SettingsRepository
import com.example.ui.components.StatusPill
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.ObsidianCardBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    settingsRepository: SettingsRepository,
    onBack: () -> Unit
) {
    val settings by settingsRepository.settings.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("settings_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_button")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "MYRAA Settings",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Preferences & System Integration",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Device Profile Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ObsidianCardBorder, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "PRIMARY PROFILE",
                            color = CyberCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Primary User: ${settings.userName}",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Target Hardware: realme 12 Pro 5G (Android 14+)",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Language Blend: Hindi + Hinglish + English",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }

                // Assistant Active Toggle
                SettingsToggleCard(
                    title = "Assistant Active",
                    description = "When enabled, MYRAA is responsive to touch, voice, and wake commands.",
                    isChecked = settings.isAssistantActive,
                    onCheckedChange = { settingsRepository.updateAssistantActive(it) }
                )

                // Background Mode Toggle
                SettingsToggleCard(
                    title = "Background Operations Mode",
                    description = "Keep MYRAA running as an Android Foreground Service with quick notification access.",
                    isChecked = settings.isBackgroundModeEnabled,
                    onCheckedChange = { settingsRepository.updateBackgroundMode(it) }
                )

                // Memory Vault Toggle
                SettingsToggleCard(
                    title = "Memory Vault (Room DB)",
                    description = "Allow MYRAA to remember user preferences and facts locally on device.",
                    isChecked = settings.isMemoryEnabled,
                    onCheckedChange = { settingsRepository.updateMemoryEnabled(it) }
                )

                // Camera Vision Toggle
                SettingsToggleCard(
                    title = "Camera Vision",
                    description = "Enable camera preview and Gemini multimodal physical world analysis.",
                    isChecked = settings.isVisionEnabled,
                    onCheckedChange = { settingsRepository.updateVisionEnabled(it) }
                )

                // Screen Awareness Toggle
                SettingsToggleCard(
                    title = "Screen Awareness",
                    description = "Enable screenshot and UI analysis when explicitly requested.",
                    isChecked = settings.isScreenAwarenessEnabled,
                    onCheckedChange = { settingsRepository.updateScreenAwareness(it) }
                )

                // Cloud AI Backend Info
                Card(
                    colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ObsidianCardBorder, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "AI BACKEND INTEGRATION",
                            color = CyberCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Primary Model: gemini-3.5-flash",
                            color = TextPrimary,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Local Engine: Instant Phone Controls & Actions",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "API Key: Injected securely via BuildConfig secrets",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsToggleCard(
    title: String,
    description: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ObsidianCardBorder, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF020617),
                    checkedTrackColor = CyberCyan,
                    uncheckedThumbColor = TextSecondary,
                    uncheckedTrackColor = ObsidianCardBorder
                )
            )
        }
    }
}

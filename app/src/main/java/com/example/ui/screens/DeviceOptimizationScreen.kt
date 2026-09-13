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
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.permissions.PermissionManager
import com.example.ui.components.StatusPill
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.ObsidianCardBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun DeviceOptimizationScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val permissionManager = remember { PermissionManager(context) }
    val status = permissionManager.getStatus()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("device_optimization_screen")
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
                IconButton(onClick = onBack, modifier = Modifier.testTag("optimization_back_button")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Realme 12 Pro 5G Optimization",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Realme UI & Android Background Stability",
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
                // Info Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF083344))
                        .border(1.dp, CyberCyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = "Realme UI aggressive battery management can terminate background assistants. Configure these 4 settings so MYRAA stays active without unexpected stops.",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }

                // Optimization Item 1: Battery Optimization
                OptimizationCard(
                    title = "1. Battery Usage: Don't Optimize",
                    description = "Allow MYRAA to run in the background without battery throttling.",
                    isConfigured = status.isIgnoringBatteryOptimizations,
                    icon = Icons.Default.BatteryChargingFull,
                    buttonLabel = "Open Battery Settings",
                    onClick = { permissionManager.openBatteryOptimizationSettings() }
                )

                // Optimization Item 2: Auto-Launch & App Permissions
                OptimizationCard(
                    title = "2. Auto-Launch & Background Activity",
                    description = "In App Details > Battery > select 'Unrestricted' & enable 'Allow background activity'.",
                    isConfigured = true,
                    icon = Icons.Default.Settings,
                    buttonLabel = "Open App Settings",
                    onClick = { permissionManager.openAppSettings() }
                )

                // Optimization Item 3: Accessibility Service
                OptimizationCard(
                    title = "3. Accessibility Service (Optional)",
                    description = "Required only if you want MYRAA to interact with UI buttons and settings automatically.",
                    isConfigured = status.isAccessibilityEnabled,
                    icon = Icons.Default.Accessibility,
                    buttonLabel = "Open Accessibility Settings",
                    onClick = { permissionManager.openAccessibilitySettings() }
                )

                // Optimization Item 4: Notification Access
                OptimizationCard(
                    title = "4. Notification Access (Optional)",
                    description = "Required only if you want MYRAA to read incoming alerts (e.g. WhatsApp, missed calls).",
                    isConfigured = status.isNotificationAccessEnabled,
                    icon = Icons.Default.Notifications,
                    buttonLabel = "Open Notification Access",
                    onClick = { permissionManager.openNotificationListenerSettings() }
                )

                // Realme UI Lock in Recent Apps Tip
                Card(
                    colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ObsidianCardBorder, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "💡 Realme UI Lock in Recents Tip",
                            color = CyberCyan,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "1. Swipe up to open Recent Apps overview.\n2. Tap the three dots (⋮) above the MYRAA preview card.\n3. Tap 'Lock' (🔒) so Realme UI never kills MYRAA when clearing recent apps.",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OptimizationCard(
    title: String,
    description: String,
    isConfigured: Boolean,
    icon: ImageVector,
    buttonLabel: String,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ObsidianCardBorder, RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = CyberCyan,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isConfigured) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isConfigured) Color(0xFF10B981) else Color(0xFFFFB703),
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = ObsidianSurfaceVariant),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = buttonLabel, color = CyberCyan, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

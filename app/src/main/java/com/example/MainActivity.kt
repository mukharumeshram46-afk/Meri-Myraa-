package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.DeveloperCopilotScreen
import com.example.ui.screens.DeviceOptimizationScreen
import com.example.ui.screens.MainAssistantScreen
import com.example.ui.screens.MemoryScreen
import com.example.ui.screens.PrivacyScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.VisionScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ObsidianBackground

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as MyraaApplication
        val assistantManager = app.assistantManager
        val settingsRepository = app.settingsRepository

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = ObsidianBackground
                ) {
                    RequestInitialPermissions()
                    MyraaNavHost(assistantManager = assistantManager, settingsRepository = settingsRepository)
                }
            }
        }
    }
}

@Composable
fun RequestInitialPermissions() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        // Permissions handled
    }

    LaunchedEffect(Unit) {
        val permissions = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        val needed = permissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
        if (needed.isNotEmpty()) {
            permissionLauncher.launch(needed.toTypedArray())
        }
    }
}

@Composable
fun MyraaNavHost(
    assistantManager: com.example.assistant.AssistantManager,
    settingsRepository: com.example.settings.SettingsRepository
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        composable("main") {
            MainAssistantScreen(
                assistantManager = assistantManager,
                onNavigateToVision = { navController.navigate("vision") },
                onNavigateToMemory = { navController.navigate("memory") },
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToDeviceOptimization = { navController.navigate("optimization") },
                onNavigateToDeveloperCopilot = { navController.navigate("developer") },
                onNavigateToPrivacy = { navController.navigate("privacy") }
            )
        }

        composable("vision") {
            VisionScreen(
                assistantManager = assistantManager,
                onBack = { navController.popBackStack() }
            )
        }

        composable("memory") {
            MemoryScreen(
                assistantManager = assistantManager,
                onBack = { navController.popBackStack() }
            )
        }

        composable("settings") {
            SettingsScreen(
                settingsRepository = settingsRepository,
                onBack = { navController.popBackStack() }
            )
        }

        composable("optimization") {
            DeviceOptimizationScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable("developer") {
            DeveloperCopilotScreen(
                assistantManager = assistantManager,
                onBack = { navController.popBackStack() }
            )
        }

        composable("privacy") {
            PrivacyScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}

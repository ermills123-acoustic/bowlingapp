package com.bowlvision.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BowlVisionTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentScreen by remember { mutableStateOf("dashboard") }

                    if (currentScreen == "dashboard") {
                        DashboardScreen(onNavigateToAnalysis = { currentScreen = "analysis" })
                    } else {
                        AnalysisScreen(onNavigateBack = { currentScreen = "dashboard" })
                    }
                }
            }
        }
    }
}

// Removed Greeting

@Composable
fun BowlVisionTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = androidx.compose.material3.darkColorScheme(
            primary = androidx.compose.ui.graphics.Color(0xFF1E88E5),
            secondary = androidx.compose.ui.graphics.Color(0xFFE53935),
            background = androidx.compose.ui.graphics.Color(0xFF121212),
            surface = androidx.compose.ui.graphics.Color(0xFF1E1E1E)
        ),
        content = content
    )
}

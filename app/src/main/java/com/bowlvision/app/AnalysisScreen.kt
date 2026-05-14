package com.bowlvision.app

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AnalysisScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Simulated Camera View
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Draw AI Skeleton Overlay Simulation
                val width = size.width
                val height = size.height
                
                // Head
                drawCircle(color = Color(0xFFE53935), radius = 30f, center = Offset(width/2, height/3))
                // Spine
                drawLine(color = Color(0xFFE53935), start = Offset(width/2, height/3), end = Offset(width/2, height/2), strokeWidth = 8f)
                // Right Arm (Bowling)
                drawLine(color = Color(0xFF1E88E5), start = Offset(width/2, height/3), end = Offset(width/2 - 100f, height/2), strokeWidth = 8f)
                drawLine(color = Color(0xFF1E88E5), start = Offset(width/2 - 100f, height/2), end = Offset(width/2 - 50f, height/1.5f), strokeWidth = 8f)
            }
        }
        
        // Overlay HUD
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Text("LIVE ANALYSIS", color = Color.Red, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0x88000000)),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Form Score", color = Color.LightGray, fontSize = 12.sp)
                    Text("95", color = Color.Green, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
        
        // Voice Feedback Simulation
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .background(Color(0x881E1E1E), shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp))
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text("Great release timing!", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

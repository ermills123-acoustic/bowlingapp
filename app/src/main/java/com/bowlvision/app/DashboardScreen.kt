package com.bowlvision.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DashboardScreen(onNavigateToAnalysis: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp)
    ) {
        Text(
            text = "BowlVision AI",
            color = Color(0xFF1E88E5), // Energetic Blue
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Form Score Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Average Form Score", color = Color.LightGray, fontSize = 16.sp)
                    Text(
                        text = "--",
                        color = Color.Gray, // Changed to gray since no score yet
                        fontSize = 56.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
        
        Button(
            onClick = onNavigateToAnalysis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Start Live AI Analysis", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Personalized Drills", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(3) { index ->
                DrillCard(index)
            }
        }
    }
}

@Composable
fun DrillCard(index: Int) {
    val drills = listOf(
        "Fix Late Slide" to "Focus on starting your slide earlier.",
        "Keep Arm Close" to "Reduce horizontal deviation.",
        "Follow Through" to "Ensure hand reaches shoulder height."
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF1E88E5), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "${index + 1}", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = drills.getOrNull(index)?.first ?: "Drill", color = Color.White, fontWeight = FontWeight.Bold)
                Text(text = drills.getOrNull(index)?.second ?: "Description", color = Color.Gray, fontSize = 14.sp)
            }
        }
    }
}

package com.example.conscia.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OnboardingRoute(onGetStartedClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme

    Scaffold(containerColor = colorScheme.background) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(modifier = Modifier.weight(1.2f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Surface(modifier = Modifier.size(280.dp), shape = CircleShape, color = colorScheme.surfaceVariant.copy(alpha = 0.5f)) {}
                Card(modifier = Modifier.size(120.dp, 220.dp), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.Smartphone, contentDescription = null, modifier = Modifier.size(64.dp), tint = colorScheme.primary)
                    }
                }
            }
            Column(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text(text = "Take Control\nof Your App Usage", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = colorScheme.onSurface, textAlign = TextAlign.Center, lineHeight = 40.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Set intents and reflect on how you spend time on apps.", style = MaterialTheme.typography.bodyLarge, color = colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            }
            Button(onClick = onGetStartedClick, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(28.dp), colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)) {
                Text(text = "Get Started", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

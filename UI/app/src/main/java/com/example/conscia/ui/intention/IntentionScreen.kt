package com.example.conscia.ui.intention

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.conscia.R
import com.example.conscia.ui.theme.tintedSurface

data class IntentionOption(
    val id: String,
    val title: String,
    val imageRes: Int,
    val iconBgColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntentionRoute(
    appName: String = "YouTube",
    onBackClick: () -> Unit,
    onContinueClick: (List<String>) -> Unit,
    onSkipClick: () -> Unit = {},
    showSkipButton: Boolean = true
) {
    val options = listOf(
        IntentionOption("learning", "Learning", R.drawable.learning_illustration, Color(0xFF4285F4)),
        IntentionOption("entertainment", "Entertainment", R.drawable.entertainment_illustration, Color(0xFF6391F2)),
        IntentionOption("work", "Work", R.drawable.work_illustration, Color(0xFF4DB6AC)),
        IntentionOption("killing_time", "Killing Time", R.drawable.killing_time_illustration, Color(0xFF64B5F6))
    )

    val selectedOptions = remember { mutableStateListOf<String>() }
    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = { 
                    IconButton(onClick = onBackClick) { 
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = colorScheme.onSurface) 
                    } 
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.surface)
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier.fillMaxWidth().background(colorScheme.surface).padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Pager Indicators (Dấu chấm nhỏ như trong hình)
                Row(modifier = Modifier.padding(bottom = 16.dp)) {
                    Box(modifier = Modifier.size(24.dp, 4.dp).clip(CircleShape).background(colorScheme.outlineVariant))
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.size(24.dp, 4.dp).clip(CircleShape).background(colorScheme.onSurfaceVariant.copy(alpha = 0.5f)))
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (showSkipButton) {
                        OutlinedButton(
                            onClick = onSkipClick,
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp),
                            shape = RoundedCornerShape(32.dp),
                            border = BorderStroke(1.dp, colorScheme.primary),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = colorScheme.primary
                            )
                        ) {
                            Text("Skip", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                    
                    Button(
                        onClick = { onContinueClick(selectedOptions.toList()) },
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp),
                        shape = RoundedCornerShape(32.dp),
                        enabled = selectedOptions.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.primary,
                            disabledContainerColor = colorScheme.outlineVariant
                        )
                    ) {
                        Text("Next", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "You're opening $appName",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onSurface
                    )
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        text = "What's your intention?",
                        style = MaterialTheme.typography.titleLarge,
                        color = colorScheme.onSurface,
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.Default.Cancel, 
                        null, 
                        tint = Color(0xFFEF4444), 
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.height(40.dp))
            }

            items(options) { option ->
                val isSelected = selectedOptions.contains(option.id)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { 
                            if (isSelected) selectedOptions.remove(option.id)
                            else selectedOptions.add(option.id)
                        },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) {
                            colorScheme.tintedSurface(option.iconBgColor)
                        } else {
                            colorScheme.surfaceVariant
                        }
                    ),
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, colorScheme.primary) else null
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(option.iconBgColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = option.imageRes),
                                contentDescription = null,
                                modifier = Modifier.size(30.dp),
                                colorFilter = ColorFilter.tint(Color.White) // Làm icon thành màu trắng như hình
                            )
                        }
                        Spacer(modifier = Modifier.width(20.dp))
                        Text(
                            text = option.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Medium,
                            color = colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

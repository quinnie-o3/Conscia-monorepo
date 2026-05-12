package com.example.conscia.ui.rules

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.conscia.data.rule.RuleEntity
import com.example.conscia.ui.theme.tintedSurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulesRoute(
    onBackClick: () -> Unit,
    onCreateRuleClick: () -> Unit,
    onEditRuleClick: (Long) -> Unit,
    viewModel: RulesViewModel = viewModel()
) {
    val rules by viewModel.rulesState.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Rules", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = colorScheme.surface)
            )
        },
        floatingActionButton = {
            if (rules.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = onCreateRuleClick,
                    containerColor = colorScheme.primary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    icon = { Icon(Icons.Default.Add, null) },
                    text = { Text("Create Rule") }
                )
            }
        }
    ) { padding ->
        if (rules.isEmpty()) {
            EmptyRulesState(onCreateRuleClick, modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
            ) {
                item {
                    Text(
                        text = "Create rules to limit distractions and stay aligned with your intention.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                items(rules) { rule ->
                    RuleCard(rule, onClick = { onEditRuleClick(rule.id) })
                }
            }
        }
    }
}

@Composable
fun RuleCard(rule: RuleEntity, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (rule.trackingEnabled) {
                            colorScheme.tintedSurface(colorScheme.primary, Color(0xFFEAF5EA))
                        } else {
                            colorScheme.surfaceVariant
                        },
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Rule, null,
                    tint = if (rule.trackingEnabled) colorScheme.primary else colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(rule.appName, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    if (rule.trackingEnabled) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.size(6.dp).background(Color(0xFF22C55E), CircleShape))
                    }
                }
                Text(rule.intentionLabel, style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                
                val hours = rule.dailyLimitMinutes / 60
                val mins = rule.dailyLimitMinutes % 60
                val timeStr = buildString {
                    if (hours > 0) append("${hours}h ")
                    if (mins > 0 || hours == 0) append("${mins}m")
                    append("/day")
                }
                
                Surface(
                    color = colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        text = timeStr,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onClick) {
                Icon(Icons.Default.MoreVert, null, tint = colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun EmptyRulesState(onCreateRuleClick: () -> Unit, modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier.size(120.dp).background(colorScheme.surfaceVariant, CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.Lightbulb, null, modifier = Modifier.size(60.dp), tint = colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("No rules yet", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            "Create your first rule to control distracting apps more intentionally.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onCreateRuleClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)
        ) {
            Text("Create Rule", fontWeight = FontWeight.Bold)
        }
    }
}

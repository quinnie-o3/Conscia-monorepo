package com.example.conscia.ui.onboarding

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.conscia.ui.components.InstalledAppIcon

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun StarterRulesRoute(
    onBackClick: () -> Unit,
    onContinueClick: () -> Unit,
    viewModel: StarterRulesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

    var showCustomIntentionDialog by remember { mutableStateOf(false) }
    var customIntentionText by remember { mutableStateOf("") }
    var targetingPackage by remember { mutableStateOf<String?>(null) }

    if (showCustomIntentionDialog) {
        AlertDialog(
            onDismissRequest = { 
                showCustomIntentionDialog = false
                targetingPackage = null
            },
            title = { Text("New Reason") },
            text = {
                OutlinedTextField(
                    value = customIntentionText,
                    onValueChange = { customIntentionText = it },
                    label = { Text("Enter your intention") },
                    placeholder = { Text("e.g. Learning English") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customIntentionText.isNotBlank() && targetingPackage != null) {
                            viewModel.createCustomIntention(customIntentionText, targetingPackage!!)
                            customIntentionText = ""
                            showCustomIntentionDialog = false
                            targetingPackage = null
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showCustomIntentionDialog = false
                    targetingPackage = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Choose Reasons", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = colorScheme.surface,
                shadowElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Starter rules use a default limit of 15 minutes/day. You can edit this later in Rules.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = { viewModel.saveStarterRules(onContinueClick) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = uiState.canContinue || !uiState.hasSelectedApps,
                        colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White
                            )
                        } else {
                            Text(
                                text = if (uiState.hasSelectedApps) "Continue" else "Skip for now",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = colorScheme.primary)
                }
            }

            !uiState.hasSelectedApps -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No apps were selected. Continue to finish setup and create rules later.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 24.dp, bottom = 120.dp)
                ) {
                    item {
                        Text(
                            text = "Tell Conscia why you want to track each app.",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Your selected reason will be saved into Rules and can be edited later.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onSurfaceVariant
                        )
                    }

                    if (uiState.errorMessage != null) {
                        item {
                            Text(
                                text = uiState.errorMessage!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFDC2626)
                            )
                        }
                    }

                    items(uiState.drafts, key = { it.packageName }) { draft ->
                        StarterRuleCard(
                            draft = draft,
                            options = uiState.availableIntentions,
                            onIntentionSelected = { intention ->
                                viewModel.onIntentionSelected(draft.packageName, intention)
                            },
                            onAddCustomIntention = {
                                targetingPackage = draft.packageName
                                showCustomIntentionDialog = true
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StarterRuleCard(
    draft: StarterRuleDraft,
    options: List<String>,
    onIntentionSelected: (String) -> Unit,
    onAddCustomIntention: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            androidx.compose.foundation.layout.Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                InstalledAppIcon(
                    packageName = draft.packageName,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
                Column(modifier = Modifier.padding(start = 14.dp)) {
                    Text(draft.appName, fontWeight = FontWeight.Bold)
                    Text(
                        draft.packageName,
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Reason",
                style = MaterialTheme.typography.labelLarge,
                color = colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                options.forEach { option ->
                    FilterChip(
                        selected = draft.intentionLabel == option,
                        onClick = { onIntentionSelected(option) },
                        label = { Text(option) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colorScheme.primary.copy(alpha = 0.12f),
                            selectedLabelColor = colorScheme.primary
                        )
                    )
                }
                
                // Exact label match as requested
                FilterChip(
                    selected = false,
                    onClick = onAddCustomIntention,
                    label = { 
                        androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Other reason -->")
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp).padding(start = 4.dp))
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        labelColor = colorScheme.onSecondaryContainer
                    )
                )
            }
        }
    }
}

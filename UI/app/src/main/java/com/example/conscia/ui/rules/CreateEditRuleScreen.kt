package com.example.conscia.ui.rules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

private val intentionSuggestions = listOf(
    "Stay focused on work",
    "Stay focused on study",
    "Avoid mindless scrolling",
    "Check updates without getting stuck",
    "Use it intentionally to relax",
    "Limit late-night usage"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateEditRuleScreen(
    ruleId: Long? = null,
    onBackClick: () -> Unit,
    onSelectAppClick: () -> Unit,
    selectedAppPackageName: String = "",
    selectedAppName: String = "",
    onSelectedAppConsumed: () -> Unit = {},
    viewModel: CreateEditRuleViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colorScheme = MaterialTheme.colorScheme
    var currentOptionStep by remember { mutableIntStateOf(0) } // 0 = Tracking, 1 = Warning, 2 = Done

    LaunchedEffect(ruleId) {
        if (ruleId != null && ruleId != -1L) {
            viewModel.loadRule(ruleId)
            currentOptionStep = 2 // In edit mode, we show all options or assume steps are done
        }
    }

    LaunchedEffect(selectedAppPackageName, selectedAppName) {
        if (selectedAppPackageName.isNotBlank()) {
            viewModel.onAppSelected(selectedAppPackageName, selectedAppName)
            onSelectedAppConsumed()
        }
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onBackClick()
        }
    }

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (uiState.isEditMode) "Edit Rule" else "Create Rule", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.isEditMode) {
                        IconButton(onClick = { viewModel.deleteRule() }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. App Selection
            val appError = !uiState.isAppValid && uiState.showErrors
            SectionTitle("App to track", isError = appError)
            Card(
                onClick = onSelectAppClick,
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (appError) Color(0xFFFFF2F2) else colorScheme.surfaceVariant
                ),
                border = if (appError) BorderStroke(1.dp, Color.Red) else null
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (appError) Icons.Default.ErrorOutline else Icons.Default.Apps,
                        contentDescription = null,
                        tint = if (appError) Color.Red else colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = if (uiState.selectedAppName.isEmpty()) "Choose an app" else uiState.selectedAppName,
                            fontWeight = FontWeight.Bold,
                            color = if (appError) Color.Red else colorScheme.onSurface
                        )
                        if (uiState.selectedPackageName.isNotEmpty()) {
                            Text(uiState.selectedPackageName, style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // 2. Your intention
            val intentionError = !uiState.isIntentionValid && uiState.showErrors
            SectionTitle("Your intention", isError = intentionError)
            OutlinedTextField(
                value = uiState.intention,
                onValueChange = { viewModel.onIntentionChanged(it) },
                label = { Text("Why do you want to track this app?") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                isError = intentionError,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = colorScheme.primary,
                    unfocusedIndicatorColor = colorScheme.outlineVariant,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    errorContainerColor = Color.Transparent
                )
            )
            Text(
                text = "Quick suggestions",
                style = MaterialTheme.typography.labelLarge,
                color = colorScheme.onSurfaceVariant
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                intentionSuggestions.forEach { suggestion ->
                    FilterChip(
                        selected = uiState.intention == suggestion,
                        onClick = { viewModel.onIntentionChanged(suggestion) },
                        label = { Text(suggestion) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colorScheme.primary.copy(alpha = 0.12f),
                            selectedLabelColor = colorScheme.primary
                        )
                    )
                }
            }

            // 3. Daily limit
            val limitError = !uiState.isLimitValid && uiState.showErrors
            SectionTitle("Daily limit", isError = limitError)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = uiState.limitHours,
                    onValueChange = { viewModel.onLimitHoursChanged(it) },
                    label = { Text("Hours") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(16.dp),
                    isError = limitError,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        errorContainerColor = Color.Transparent
                    )
                )
                OutlinedTextField(
                    value = uiState.limitMinutes,
                    onValueChange = { viewModel.onLimitMinutesChanged(it) },
                    label = { Text("Minutes") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(16.dp),
                    isError = limitError,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        errorContainerColor = Color.Transparent
                    )
                )
            }
            if (limitError) {
                Text("Please set a limit greater than 0", color = Color.Red, style = MaterialTheme.typography.bodySmall)
            }

            // 4. Options
            SectionTitle("Options")
            if (!uiState.isEditMode && currentOptionStep < 2) {
                // Stepper for new rules
                if (currentOptionStep == 0) {
                    OptionStepCard(
                        title = "Enable tracking?",
                        subtitle = "Track app usage to help manage your time",
                        onDisable = { currentOptionStep = 1 },
                        onEnable = {
                            viewModel.onTrackingEnabledChanged(true)
                            currentOptionStep = 1
                        }
                    )
                } else {
                    OptionStepCard(
                        title = "Enable warnings?",
                        subtitle = "Show warnings when approaching time limit",
                        onDisable = { currentOptionStep = 2 },
                        onEnable = {
                            viewModel.onWarningEnabledChanged(true)
                            currentOptionStep = 2
                        }
                    )
                }
            } else {
                // Regular switches for edit mode or finished steps
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        OptionRow("Tracking enabled", uiState.trackingEnabled, viewModel::onTrackingEnabledChanged)
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = colorScheme.outlineVariant)
                        OptionRow("Warning enabled", uiState.warningEnabled, viewModel::onWarningEnabledChanged)
                    }
                }
            }

            if (uiState.errorMessage != null) {
                Text(uiState.errorMessage!!, color = Color.Red, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.weight(1f))

            // Save Button
            Button(
                onClick = { viewModel.saveRule() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (uiState.isFormValid) colorScheme.primary else colorScheme.outline.copy(alpha = 0.5f)
                ),
                enabled = uiState.isFormValid && !uiState.isSaving
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    val label = if (uiState.isEditMode) "Update Rule" else "Save Rule"
                    Text(label, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun OptionStepCard(
    title: String,
    subtitle: String,
    onDisable: () -> Unit,
    onEnable: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(bottom = 8.dp))
            Text(subtitle, color = colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDisable,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, colorScheme.primary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colorScheme.primary)
                ) {
                    Text("Disable", fontWeight = FontWeight.Medium)
                }
                Button(
                    onClick = onEnable,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)
                ) {
                    Text("Enable", fontWeight = FontWeight.Medium, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun SectionTitle(text: String, isError: Boolean = false) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = if (isError) Color.Red else MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
fun OptionRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.padding(16.dp).fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontWeight = FontWeight.Medium)
        Switch(
            checked = checked, 
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

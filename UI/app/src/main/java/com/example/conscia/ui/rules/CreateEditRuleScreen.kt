package com.example.conscia.ui.rules

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateEditRuleScreen(
    ruleId: Long? = null,
    onBackClick: () -> Unit,
    onSelectAppClick: () -> Unit,
    selectedAppPackageName: String = "",
    selectedAppName: String = "",
    onSelectedAppConsumed: () -> Unit = {},
    viewModel: CreateEditRuleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colorScheme = MaterialTheme.colorScheme
    var showLimitPicker by remember { mutableStateOf(false) }

    LaunchedEffect(ruleId) {
        if (ruleId != null && ruleId != -1L) {
            viewModel.loadRule(ruleId)
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

    if (showLimitPicker) {
        LimitTimePickerDialog(
            selectedHour = uiState.limitHourValue,
            selectedMinute = uiState.limitMinuteValue,
            onHourSelected = viewModel::setLimitHour,
            onMinuteSelected = viewModel::setLimitMinute,
            onDismiss = { showLimitPicker = false }
        )
    }

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (uiState.isEditMode) "Edit Rule" else "Add Rule", fontWeight = FontWeight.Bold) },
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
            SectionTitle("1. Select app to track", isError = appError)
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
                            text = if (uiState.selectedAppName.isEmpty()) "Select an app" else uiState.selectedAppName,
                            fontWeight = FontWeight.Bold,
                            color = if (appError) Color.Red else colorScheme.onSurface
                        )
                        if (uiState.selectedPackageName.isNotEmpty()) {
                            Text(uiState.selectedPackageName, style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // 2. Reasons
            val intentionError = !uiState.isIntentionValid && uiState.showErrors
            SectionTitle("2. Reasons", isError = intentionError)
            
            Text(
                text = "Choose a prepared reason or type your own.",
                style = MaterialTheme.typography.labelLarge,
                color = colorScheme.onSurfaceVariant
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.availableIntentions.forEach { suggestion ->
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

            OutlinedTextField(
                value = uiState.intention,
                onValueChange = { viewModel.onIntentionChanged(it) },
                label = { Text("Reason") },
                placeholder = { Text("e.g. Study, sleep earlier, focus work") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                isError = intentionError,
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = colorScheme.primary,
                    unfocusedIndicatorColor = colorScheme.outlineVariant,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    errorContainerColor = Color.Transparent
                )
            )
            if (intentionError) {
                Text("Please choose or enter a reason", color = Color.Red, style = MaterialTheme.typography.bodySmall)
            }

            // 3. Limit time
            val limitError = !uiState.isLimitValid && uiState.showErrors
            SectionTitle("3. Set limit time", isError = limitError)
            LimitTimeSelector(
                hours = uiState.limitHours,
                minutes = uiState.limitMinutes,
                isError = limitError,
                onClick = { showLimitPicker = true }
            )
            if (limitError) {
                Text("Minimum tracking limit is 00:01. Minutes must be between 00 and 59.", color = Color.Red, style = MaterialTheme.typography.bodySmall)
            }

            // 4. Options
            SectionTitle("Options")
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

            if (uiState.errorMessage != null) {
                Text(uiState.errorMessage!!, color = Color.Red, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(24.dp))

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
                    val label = if (uiState.isEditMode) "Save Changes" else "Add Rule"
                    Text(label, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun LimitTimeSelector(
    hours: String,
    minutes: String,
    isError: Boolean,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        border = if (isError) BorderStroke(1.dp, Color.Red) else BorderStroke(1.dp, colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, contentDescription = null, tint = if (isError) Color.Red else colorScheme.primary)
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = "$hours:$minutes",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isError) Color.Red else colorScheme.onSurface
                )
            }
            Text(
                text = "HH:MM",
                style = MaterialTheme.typography.labelLarge,
                color = colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun LimitTimePickerDialog(
    selectedHour: Int,
    selectedMinute: Int,
    onHourSelected: (Int) -> Unit,
    onMinuteSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set limit time") },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth().height(320.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TimePickerColumn(
                    title = "Hour",
                    values = (0..23).toList(),
                    selectedValue = selectedHour,
                    formatter = { it.toString().padStart(2, '0') },
                    onValueSelected = onHourSelected,
                    modifier = Modifier.weight(1f)
                )
                TimePickerColumn(
                    title = "Minute",
                    values = (0..59).toList(),
                    selectedValue = selectedMinute,
                    formatter = { it.toString().padStart(2, '0') },
                    onValueSelected = onMinuteSelected,
                    modifier = Modifier.weight(1f)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)
            ) {
                Text("Done")
            }
        }
    )
}

@Composable
fun TimePickerColumn(
    title: String,
    values: List<Int>,
    selectedValue: Int,
    formatter: (Int) -> String,
    onValueSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge, color = colorScheme.onSurfaceVariant)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(values) { value ->
                val selected = value == selectedValue
                Surface(
                    modifier = Modifier.fillMaxWidth().clickable { onValueSelected(value) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (selected) colorScheme.primary else colorScheme.surfaceVariant,
                    contentColor = if (selected) Color.White else colorScheme.onSurface
                ) {
                    Text(
                        text = formatter(value),
                        modifier = Modifier.padding(vertical = 10.dp),
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
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

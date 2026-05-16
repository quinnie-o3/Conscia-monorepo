@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.conscia.ui.tracked

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.example.conscia.domain.model.TrackedAppLimitInfo
import com.example.conscia.util.TimeFormatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackedAppsRoute(
    onBackClick: () -> Unit,
    onAppClick: (Long) -> Unit,
    viewModel: TrackedAppsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.load()
        }
    }

    TrackedAppsContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onAppClick = onAppClick
    )
}

@Composable
private fun TrackedAppsContent(
    uiState: TrackedAppsUiState,
    onBackClick: () -> Unit,
    onAppClick: (Long) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Tracked Apps", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = colorScheme.surface)
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.apps.isEmpty() -> {
                EmptyTrackedApps(modifier = Modifier.padding(padding))
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
                ) {
                    item {
                        Text(
                            text = "Apps currently monitored by your rules.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                    items(uiState.apps) { app ->
                        TrackedAppRow(app = app, onClick = { onAppClick(app.ruleId) })
                    }
                }
            }
        }
    }
}

@Composable
private fun TrackedAppRow(app: TrackedAppLimitInfo, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).background(colorScheme.primary, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(app.appName.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(app.appName, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(app.intentionLabel, style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { app.usagePercent.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                    color = colorScheme.primary,
                    trackColor = colorScheme.surfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${TimeFormatters.formatDurationShort(app.todayUsageMillis)} / ${TimeFormatters.formatDurationDailyLimit(app.dailyLimitMinutes)} today",
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.ChevronRight, null, tint = colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun EmptyTrackedApps(modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(96.dp).background(colorScheme.surfaceVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.TrackChanges, null, modifier = Modifier.size(44.dp), tint = colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text("No tracked apps", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(
            "Create a rule to start tracking an app.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackedAppDetailRoute(
    ruleId: Long,
    onBackClick: () -> Unit,
    viewModel: TrackedAppDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(ruleId) {
        viewModel.load(ruleId)
    }

    TrackedAppDetailContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onStartLimitEdit = viewModel::startLimitEdit,
        onStartReasonEdit = viewModel::startReasonEdit,
        onLimitHoursChanged = viewModel::onLimitHoursChanged,
        onLimitMinutesChanged = viewModel::onLimitMinutesChanged,
        onReasonChanged = viewModel::onReasonChanged,
        onSaveLimit = viewModel::saveLimit,
        onSaveReason = viewModel::saveReason,
        onCancelLimit = viewModel::cancelLimitEdit,
        onCancelReason = viewModel::cancelReasonEdit
    )
}

@Composable
private fun TrackedAppDetailContent(
    uiState: TrackedAppDetailUiState,
    onBackClick: () -> Unit,
    onStartLimitEdit: () -> Unit,
    onStartReasonEdit: () -> Unit,
    onLimitHoursChanged: (String) -> Unit,
    onLimitMinutesChanged: (String) -> Unit,
    onReasonChanged: (String) -> Unit,
    onSaveLimit: () -> Unit,
    onSaveReason: () -> Unit,
    onCancelLimit: () -> Unit,
    onCancelReason: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val rule = uiState.rule

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(rule?.appName ?: "Tracked App", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = colorScheme.surface)
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            rule == null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(uiState.errorMessage ?: "Tracked app not found.", textAlign = TextAlign.Center)
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 20.dp, bottom = 36.dp)
                ) {
                    item {
                        AppUsageSummaryCard(uiState)
                    }
                    if (uiState.errorMessage != null) {
                        item {
                            Text(
                                text = uiState.errorMessage,
                                color = Color(0xFFB91C1C),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    item {
                        EditableLimitCard(
                            uiState = uiState,
                            onEdit = onStartLimitEdit,
                            onHoursChanged = onLimitHoursChanged,
                            onMinutesChanged = onLimitMinutesChanged,
                            onSave = onSaveLimit,
                            onCancel = onCancelLimit
                        )
                    }
                    item {
                        EditableReasonCard(
                            uiState = uiState,
                            onEdit = onStartReasonEdit,
                            onReasonChanged = onReasonChanged,
                            onSave = onSaveReason,
                            onCancel = onCancelReason
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppUsageSummaryCard(uiState: TrackedAppDetailUiState) {
    val colorScheme = MaterialTheme.colorScheme
    val rule = uiState.rule ?: return
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(52.dp).background(colorScheme.primary, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(rule.appName.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(rule.appName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(rule.packageName, style = MaterialTheme.typography.bodySmall, color = colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "${TimeFormatters.formatDurationShort(uiState.todayUsageMillis)} / ${TimeFormatters.formatDurationDailyLimit(rule.dailyLimitMinutes)} today",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colorScheme.primary
            )
            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { uiState.usagePercent.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
                color = colorScheme.primary,
                trackColor = colorScheme.surface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun EditableLimitCard(
    uiState: TrackedAppDetailUiState,
    onEdit: () -> Unit,
    onHoursChanged: (String) -> Unit,
    onMinutesChanged: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    EditableInfoCard(title = "Daily Limit", onEdit = onEdit, isEditing = uiState.isEditingLimit) {
        if (uiState.isEditingLimit) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = uiState.limitHours,
                    onValueChange = onHoursChanged,
                    label = { Text("Hours") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = uiState.limitMinutes,
                    onValueChange = onMinutesChanged,
                    label = { Text("Minutes") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
            EditActions(onSave = onSave, onCancel = onCancel)
        } else {
            Text(
                text = TimeFormatters.formatDurationDailyLimit(uiState.savedLimitMinutes),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun EditableReasonCard(
    uiState: TrackedAppDetailUiState,
    onEdit: () -> Unit,
    onReasonChanged: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    EditableInfoCard(title = "Reason", onEdit = onEdit, isEditing = uiState.isEditingReason) {
        if (uiState.isEditingReason) {
            OutlinedTextField(
                value = uiState.reason,
                onValueChange = onReasonChanged,
                label = { Text("Reason") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            EditActions(onSave = onSave, onCancel = onCancel)
        } else {
            Text(
                text = uiState.savedReason,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun EditableInfoCard(
    title: String,
    onEdit: () -> Unit,
    isEditing: Boolean,
    content: @Composable ColumnScope.() -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                if (!isEditing) {
                    TextButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Edit")
                    }
                }
            }
            content()
        }
    }
}

@Composable
private fun EditActions(onSave: () -> Unit, onCancel: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Cancel")
        }
        Button(
            onClick = onSave,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

package com.example.conscia.ui.insights

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.conscia.ui.dashboard.PermissionRequiredView
import com.example.conscia.ui.theme.tintedSurface
import com.example.conscia.util.TimeFormatters
import kotlinx.coroutines.delay

@Composable
fun InsightsRoute(viewModel: InsightsViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner, uiState.weekOffset) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while (true) {
                viewModel.refresh()
                delay(15_000)
            }
        }
    }

    InsightsContent(
        uiState = uiState,
        onPreviousWeekClick = viewModel::showPreviousWeek,
        onNextWeekClick = viewModel::showNextWeek,
        onRefreshClick = { viewModel.refresh(showLoading = true) },
        onGrantPermissionClick = viewModel::onGrantUsageAccessClicked
    )
}

@Composable
private fun InsightsContent(
    uiState: InsightsUiState,
    onPreviousWeekClick: () -> Unit,
    onNextWeekClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onGrantPermissionClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Scaffold(containerColor = colorScheme.background) { padding ->
        when {
            !uiState.hasUsagePermission && !uiState.isLoading -> {
                PermissionRequiredView(
                    onGrantClick = onGrantPermissionClick,
                    modifier = Modifier.padding(padding)
                )
            }

            uiState.isLoading && uiState.lastUpdatedLabel.isBlank() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = colorScheme.primary)
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(
                                    text = "Insights",
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (uiState.lastUpdatedLabel.isBlank()) {
                                        "Refreshes every 15s while this screen is open"
                                    } else {
                                        "Last updated ${uiState.lastUpdatedLabel} • refreshes every 15s"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colorScheme.onSurfaceVariant
                                )
                            }

                            Surface(
                                color = colorScheme.tintedSurface(colorScheme.primary),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    text = "Live",
                                    color = colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = uiState.dateRangeLabel,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Row(
                                modifier = Modifier
                                    .background(colorScheme.surfaceVariant, RoundedCornerShape(20.dp))
                                    .padding(horizontal = 4.dp)
                            ) {
                                IconButton(onClick = onPreviousWeekClick, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null, tint = colorScheme.primary)
                                }
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(16.dp)
                                        .background(colorScheme.outlineVariant)
                                        .align(Alignment.CenterVertically)
                                )
                                IconButton(
                                    onClick = onNextWeekClick,
                                    modifier = Modifier.size(32.dp),
                                    enabled = uiState.weekOffset > 0
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        null,
                                        tint = if (uiState.weekOffset > 0) colorScheme.primary else colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                    )
                                }
                            }
                        }
                    }

                    if (uiState.errorMessage != null) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = colorScheme.tintedSurface(Color(0xFFEF4444)))
                            ) {
                                Text(
                                    text = uiState.errorMessage,
                                    color = Color(0xFFEF4444),
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            InsightsDonutChart(
                                purposefulPercent = uiState.purposefulPercent,
                                trackedAppsCount = uiState.trackedAppsCount
                            )

                            Spacer(modifier = Modifier.width(20.dp))

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                InsightMetric(
                                    label = "Tracked usage",
                                    value = TimeFormatters.formatDurationShort(uiState.purposefulUsageMillis),
                                    valueColor = colorScheme.primary
                                )
                                HorizontalDivider(color = colorScheme.outlineVariant)
                                InsightMetric(
                                    label = "Avg / day",
                                    value = TimeFormatters.formatDurationShort(uiState.averageDailyUsageMillis),
                                    valueColor = colorScheme.secondary
                                )
                                HorizontalDivider(color = colorScheme.outlineVariant)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Schedule,
                                        null,
                                        tint = colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Other usage",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = TimeFormatters.formatDurationShort(uiState.otherUsageMillis),
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(24.dp)) {
                                Text(
                                    text = "Weekly Reflection",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = uiState.reflectionText,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = colorScheme.onSurfaceVariant,
                                    lineHeight = 24.sp
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Button(
                                    onClick = onRefreshClick,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    shape = RoundedCornerShape(28.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)
                                ) {
                                    Text("Refresh Now", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(32.dp)) }
                }
            }
        }
    }
}

@Composable
private fun InsightMetric(label: String, value: String, valueColor: Color) {
    val colorScheme = MaterialTheme.colorScheme

    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = valueColor
        )
    }
}

@Composable
private fun InsightsDonutChart(purposefulPercent: Int, trackedAppsCount: Int) {
    val colorScheme = MaterialTheme.colorScheme
    val clampedPercent = purposefulPercent.coerceIn(0, 100)
    val sweepAngle = clampedPercent / 100f * 360f

    Box(
        modifier = Modifier.size(160.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(120.dp)) {
            val strokeWidth = 20.dp.toPx()
            drawCircle(
                color = colorScheme.surfaceVariant,
                style = Stroke(width = strokeWidth)
            )

            when {
                sweepAngle >= 360f -> drawCircle(
                    color = colorScheme.primary,
                    style = Stroke(width = strokeWidth)
                )

                sweepAngle > 0f -> drawArc(
                    color = colorScheme.primary,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${clampedPercent}%",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (trackedAppsCount == 0) "Untracked" else "Tracked",
                fontSize = 12.sp,
                color = colorScheme.onSurfaceVariant
            )
        }
    }
}

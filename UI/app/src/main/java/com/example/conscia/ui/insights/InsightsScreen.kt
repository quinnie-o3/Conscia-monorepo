package com.example.conscia.ui.insights

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.example.conscia.ui.dashboard.PermissionRequiredView
import com.example.conscia.ui.theme.appUsageChartColor
import com.example.conscia.ui.theme.tintedSurface
import com.example.conscia.util.TimeFormatters
import kotlinx.coroutines.delay

@Composable
fun InsightsRoute(viewModel: InsightsViewModel = hiltViewModel()) {
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
                        Column(modifier = Modifier.fillMaxWidth()) {
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
                                    "Last updated ${uiState.lastUpdatedLabel} | refreshes every 15s"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorScheme.onSurfaceVariant
                            )
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
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                InsightsDonutChart(rankings = uiState.appUsageRankings)

                                Spacer(modifier = Modifier.width(20.dp))

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    InsightMetric(
                                        label = "Total usage",
                                        value = TimeFormatters.formatDurationShort(uiState.totalUsageMillis),
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

                            Spacer(modifier = Modifier.height(18.dp))
                            AppUsageLegend(rankings = uiState.appUsageRankings)
                        }
                    }

                    item {
                        AppUsageBarChart(rankings = uiState.appUsageRankings)
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
private fun AppUsageBarChart(rankings: List<AppUsageRanking>) {
    val colorScheme = MaterialTheme.colorScheme
    val maxUsageMillis = rankings.maxOfOrNull { it.usageMillis } ?: 0L

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "App Usage Ranking",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Sorted by time used, highest to lowest.",
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(20.dp))

            if (rankings.isEmpty() || maxUsageMillis == 0L) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No app usage detected for this range.",
                        color = colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
                return@Column
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .height(260.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                rankings.forEachIndexed { index, app ->
                    val ratio = app.usageMillis.toFloat() / maxUsageMillis.toFloat()
                    val barHeight = (150.dp.value * ratio.coerceIn(0.04f, 1f)).dp

                    Column(
                        modifier = Modifier.width(64.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            text = "${TimeFormatters.formatDurationShort(app.usageMillis)} | ${app.usagePercent}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(barHeight)
                                .background(
                                    appUsageChartColor(index),
                                    RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                                )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = app.appName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.height(32.dp)
                        )
                        if (app.limitMinutes != null) {
                            Text(
                                text = "Limit ${TimeFormatters.formatDurationDailyLimit(app.limitMinutes)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppUsageLegend(rankings: List<AppUsageRanking>) {
    val colorScheme = MaterialTheme.colorScheme

    if (rankings.isEmpty()) {
        Text(
            text = "No weekly app usage to calculate yet.",
            style = MaterialTheme.typography.bodySmall,
            color = colorScheme.onSurfaceVariant
        )
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rankings.forEachIndexed { index, app ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(appUsageChartColor(index), RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = app.appName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${app.usagePercent}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = TimeFormatters.formatDurationShort(app.usageMillis),
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant
                )
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
private fun InsightsDonutChart(rankings: List<AppUsageRanking>) {
    val colorScheme = MaterialTheme.colorScheme
    val hasUsage = rankings.any { it.usageMillis > 0L }

    Box(
        modifier = Modifier.size(160.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(120.dp)) {
            val strokeWidth = 20.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2f
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

            drawCircle(
                color = colorScheme.surfaceVariant,
                radius = radius,
                style = Stroke(width = strokeWidth)
            )

            if (hasUsage) {
                var startAngle = -90f
                var consumedAngle = 0f
                rankings.forEachIndexed { index, app ->
                    val sweepAngle = if (index == rankings.lastIndex) {
                        360f - consumedAngle
                    } else {
                        app.usagePercent.coerceAtLeast(0) / 100f * 360f
                    }

                    if (sweepAngle > 0f) {
                        drawArc(
                            color = appUsageChartColor(index),
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth)
                        )
                    }

                    startAngle += sweepAngle
                    consumedAngle += sweepAngle
                }
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (hasUsage) "100%" else "0%",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Apps",
                fontSize = 12.sp,
                color = colorScheme.onSurfaceVariant
            )
        }
    }
}

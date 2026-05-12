package com.example.conscia.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.conscia.domain.model.TrackedAppLimitInfo
import com.example.conscia.domain.model.UsageLimitStatus
import com.example.conscia.model.AppUsageInfo
import com.example.conscia.ui.theme.tintedSurface
import com.example.conscia.util.TimeFormatters

@Composable
fun DashboardRoute(viewModel: DashboardViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.refresh()
        }
    }

    DashboardContent(
        uiState = uiState,
        onGrantPermissionClick = { viewModel.onGrantUsageAccessClicked() }
    )
}

@Composable
fun DashboardContent(
    uiState: DashboardUiState,
    onGrantPermissionClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colorScheme.background,
        floatingActionButton = {
            if (uiState.hasUsagePermission) {
                ExtendedFloatingActionButton(
                    onClick = { /* TODO */ },
                    containerColor = colorScheme.primary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    icon = { Icon(Icons.Default.Add, "Add") },
                    text = { Text("New Goal") }
                )
            }
        }
    ) { padding ->
        if (!uiState.hasUsagePermission) {
            PermissionRequiredView(onGrantPermissionClick, modifier = Modifier.padding(padding))
        } else if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colorScheme.primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    UsageDonutOverviewCard(
                        totalMillis = uiState.totalTodayUsageMillis,
                        trackedMillis = uiState.trackedTodayUsageMillis,
                        otherMillis = uiState.otherTodayUsageMillis,
                        exceededCount = uiState.exceededCount,
                        nearLimitCount = uiState.nearLimitCount
                    )
                }

                if (uiState.trackedAppStatuses.isNotEmpty()) {
                    item { SectionHeader("Rules Today") }
                    items(uiState.trackedAppStatuses) { statusInfo ->
                        TrackedAppStatusItem(statusInfo)
                    }
                }
                
                if (uiState.todayTopApps.isNotEmpty()) {
                    item { SectionHeader("Today's App Usage") }
                    items(uiState.todayTopApps) { usage ->
                        UsageItem(usage)
                    }
                }

                item {
                    SectionHeader("Weekly Summary")
                    WeeklyPreviewCard(
                        totalMillis = uiState.weeklyTotalUsageMillis,
                        label = uiState.weeklySummaryLabel,
                        isLockedSnapshot = uiState.hasLockedWeeklySummary
                    )
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun TrackedAppStatusItem(info: TrackedAppLimitInfo) {
    val colorScheme = MaterialTheme.colorScheme
    val statusColor = when (info.status) {
        UsageLimitStatus.EXCEEDED -> Color(0xFFEF4444)
        UsageLimitStatus.NEAR_LIMIT -> Color(0xFFF59E0B)
        UsageLimitStatus.NORMAL -> colorScheme.primary
    }

    val statusLabel = when (info.status) {
        UsageLimitStatus.EXCEEDED -> "Limit exceeded"
        UsageLimitStatus.NEAR_LIMIT -> "Almost at limit"
        UsageLimitStatus.NORMAL -> "On track"
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (info.status) {
                UsageLimitStatus.EXCEEDED -> colorScheme.tintedSurface(Color(0xFFEF4444), Color(0xFFFEF2F2))
                UsageLimitStatus.NEAR_LIMIT -> colorScheme.tintedSurface(Color(0xFFF59E0B), Color(0xFFFFFBEB))
                UsageLimitStatus.NORMAL -> colorScheme.tintedSurface(colorScheme.primary, Color(0xFFEAF5EA))
            }
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(colorScheme.surface, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(info.appName.take(1).uppercase(), fontWeight = FontWeight.Bold, color = statusColor)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(info.appName, fontWeight = FontWeight.Bold)
                    Text(info.intentionLabel, style = MaterialTheme.typography.bodySmall, color = statusColor.copy(alpha = 0.7f))
                }
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = statusLabel,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "Used ${TimeFormatters.formatDurationShort(info.todayUsageMillis)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Limit: ${TimeFormatters.formatDurationDailyLimit(info.dailyLimitMinutes)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = { info.usagePercent.coerceAtMost(1.0f) },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = statusColor,
                trackColor = colorScheme.surface
            )
        }
    }
}

@Composable
fun UsageDonutOverviewCard(
    totalMillis: Long,
    trackedMillis: Long,
    otherMillis: Long,
    exceededCount: Int,
    nearLimitCount: Int
) {
    val colorScheme = MaterialTheme.colorScheme
    val trackedPercent = if (totalMillis > 0) {
        ((trackedMillis.toFloat() / totalMillis.toFloat()) * 100f).toInt().coerceIn(0, 100)
    } else {
        0
    }
    val warningSummary = when {
        exceededCount > 0 && nearLimitCount > 0 -> "$exceededCount app exceeded, $nearLimitCount near limit"
        exceededCount > 0 -> "$exceededCount app exceeded"
        nearLimitCount > 0 -> "$nearLimitCount near limit"
        else -> ""
    }

    Card(
        shape = RoundedCornerShape(40.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.tintedSurface(colorScheme.primary, Color(0xFFEAF5EA))
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UsageDonutChart(
                percent = trackedPercent,
                modifier = Modifier.size(148.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Total screen time",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colorScheme.onSurface
                )
                Text(
                    text = TimeFormatters.formatDurationShort(totalMillis),
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                    color = colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tracked: ${TimeFormatters.formatDurationShort(trackedMillis)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurface
                )
                Text(
                    text = "Other apps: ${TimeFormatters.formatDurationShort(otherMillis)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant
                )

                if (warningSummary.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (exceededCount > 0) Color(0xFFEF4444) else Color(0xFFF59E0B),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = warningSummary,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (exceededCount > 0) Color(0xFFEF4444) else Color(0xFFB45309)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "All tracked apps are within limits",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Composable
private fun UsageDonutChart(percent: Int, modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    val sweepAngle = percent.coerceIn(0, 100) / 100f * 360f

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 18.dp.toPx()
            drawCircle(
                color = colorScheme.surface,
                style = Stroke(width = strokeWidth)
            )
            if (sweepAngle > 0f) {
                drawArc(
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
                text = "${percent.coerceIn(0, 100)}%",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Tracked",
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun WeeklyPreviewCard(totalMillis: Long, label: String, isLockedSnapshot: Boolean) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = if (isLockedSnapshot) "Locked Weekly Total" else "Live Weekly Preview",
                style = MaterialTheme.typography.labelLarge,
                color = colorScheme.onSurfaceVariant
            )
            Text(
                text = TimeFormatters.formatDurationShort(totalMillis),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            if (label.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun PermissionRequiredView(onGrantClick: () -> Unit, modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(120.dp),
            shape = RoundedCornerShape(32.dp),
            color = colorScheme.tintedSurface(colorScheme.primary, Color(0xFFEAF5EA))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Usage access required",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Conscia needs usage access to show your real app activity and weekly insights.",
            style = MaterialTheme.typography.bodyMedium,
            color = colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onGrantClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)
        ) {
            Text(text = "Grant Access", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun UsageItem(usage: AppUsageInfo) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(colorScheme.surface, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = usage.appName.take(1).uppercase(),
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = usage.appName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = usage.packageName,
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = TimeFormatters.formatDurationShort(usage.totalTimeInForegroundMillis),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.primary
            )
        }
    }
}

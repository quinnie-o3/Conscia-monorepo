package com.example.conscia.ui.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.example.conscia.R
import com.example.conscia.domain.model.TrackedAppLimitInfo
import com.example.conscia.domain.model.UsageLimitStatus
import com.example.conscia.model.AppUsageInfo
import com.example.conscia.ui.theme.tintedSurface
import com.example.conscia.util.TimeFormatters

@Composable
fun DashboardRoute(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateToSettings: () -> Unit = {},
    onNewGoalClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.refresh()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            if (uiState.hasUsagePermission) {
                ExtendedFloatingActionButton(
                    onClick = onNewGoalClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    icon = { Icon(Icons.Default.Add, "Add") },
                    text = { Text("Add Rule") }
                )
            }
        }
    ) { padding ->
        DashboardContent(
            uiState = uiState,
            onGrantPermissionClick = { viewModel.onGrantUsageAccessClicked() },
            onExtendLimit = { ruleId -> viewModel.extendLimit(ruleId) },
            onProfileClick = onNavigateToSettings,
            onNewGoalClick = onNewGoalClick,
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
fun DashboardContent(
    uiState: DashboardUiState,
    onGrantPermissionClick: () -> Unit,
    onExtendLimit: (Long) -> Unit,
    onProfileClick: () -> Unit,
    onNewGoalClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    if (!uiState.hasUsagePermission) {
        PermissionRequiredView(onGrantPermissionClick, modifier = modifier)
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                UserHeader(
                    userName = uiState.userName,
                    onClick = onProfileClick
                )
            }

            item {
                UsageDonutOverviewCard(
                    totalMillis = uiState.totalTodayUsageMillis,
                    trackedMillis = uiState.trackedTodayUsageMillis,
                    otherMillis = uiState.otherTodayUsageMillis,
                    exceededCount = uiState.exceededCount,
                    nearLimitCount = uiState.nearLimitCount
                )
            }

            if (uiState.isEmpty) {
                item {
                    EmptyRulesHomeCard(onNewGoalClick = onNewGoalClick)
                }
            }

            if (uiState.trackedAppStatuses.isNotEmpty()) {
                item { SectionHeader("Rules Today") }
                items(uiState.trackedAppStatuses) { statusInfo ->
                    TrackedAppStatusItem(
                        info = statusInfo,
                        onExtendClick = { onExtendLimit(statusInfo.ruleId) }
                    )
                }
            }
            
            if (uiState.hasTrackedRules) {
                item { SectionHeader("Today's App Usage") }
                if (uiState.todayTopApps.isEmpty()) {
                    item {
                        EmptyMetricCard("No usage recorded today for apps in your rules.")
                    }
                } else {
                    items(uiState.todayTopApps) { usage ->
                        UsageItem(usage)
                    }
                }
            }

            if (uiState.hasTrackedRules) {
                item {
                    SectionHeader("Weekly Summary")
                    WeeklyPreviewCard(
                        totalMillis = uiState.weeklyTotalUsageMillis,
                        label = uiState.weeklySummaryLabel,
                        isLockedSnapshot = uiState.hasLockedWeeklySummary
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun EmptyRulesHomeCard(onNewGoalClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "No rules yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Create a rule before Home starts showing app usage, limits, and weekly totals.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onNewGoalClick,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Rule")
            }
        }
    }
}

@Composable
fun EmptyMetricCard(message: String) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun UserHeader(
    userName: String,
    onClick: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    
    Surface(
        modifier = Modifier
            .wrapContentWidth()
            .widthIn(max = screenWidth * 0.45f)
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier = Modifier.size(32.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Image(
                    painter = painterResource(id = R.drawable.default_user_avt),
                    contentDescription = "Profile",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Column {
                Text(
                    text = "Hello,",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = userName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
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
fun TrackedAppStatusItem(
    info: TrackedAppLimitInfo,
    onExtendClick: () -> Unit
) {
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

    val animatedProgress by animateFloatAsState(
        targetValue = info.usagePercent.coerceAtMost(1.0f),
        animationSpec = tween(durationMillis = 1000),
        label = "ProgressBarAnimation"
    )

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
                
                if (info.status == UsageLimitStatus.EXCEEDED) {
                    if (info.canExtend) {
                        TextButton(
                            onClick = onExtendClick,
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFBE123C)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.End) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Timer, null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Extend 5m", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                                Text("${3 - info.extensionCount} left", fontSize = 9.sp, fontWeight = FontWeight.Normal)
                            }
                        }
                    } else {
                        Surface(
                            color = Color(0xFFEF4444).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Max Extended",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEF4444)
                            )
                        }
                    }
                } else {
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
                progress = { animatedProgress },
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
                    text = "Today's rule screen time",
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
                    text = "Rules: ${TimeFormatters.formatDurationShort(trackedMillis)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurface
                )
                if (otherMillis > 0L) {
                    Text(
                        text = "Other apps: ${TimeFormatters.formatDurationShort(otherMillis)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant
                    )
                }

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
                        text = "All rules are within limits",
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
    
    val animatedPercent by animateFloatAsState(
        targetValue = percent.coerceIn(0, 100).toFloat(),
        animationSpec = tween(durationMillis = 1200),
        label = "DonutChartAnimation"
    )
    
    val sweepAngle = animatedPercent / 100f * 360f

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
                text = "${animatedPercent.toInt()}%",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Rules",
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
                    .size(40.dp)
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

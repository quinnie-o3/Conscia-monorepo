package com.example.conscia.ui.permissions

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

@Composable
fun PermissionsRoute(onContinueClick: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val primaryGreen = Color(0xFF006654)

    // State for permissions
    var isUsageGranted by remember { mutableStateOf(checkUsageStatsPermission(context)) }
    var isOverlayGranted by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    var isNotificationGranted by remember { mutableStateOf(checkNotificationAccess(context)) }
    var currentStepIndex by rememberSaveable { mutableIntStateOf(0) }
    var skippedStepIndexes by rememberSaveable { mutableStateOf(setOf<Int>()) }

    val permissionSteps = listOf(
        PermissionStep(
            title = "Usage Access",
            statusLabel = "Required",
            description = "Track time spent on selected apps",
            supportingNote = "This is used to detect how long you spend in apps. We never read your messages or private content.",
            icon = Icons.Default.QueryStats,
            isGranted = isUsageGranted,
            isRequired = true,
            onEnableClick = {
                context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            }
        ),
        PermissionStep(
            title = "Display over other apps",
            statusLabel = "Optional",
            description = "Show reminders when your limit is reached",
            supportingNote = "Used to show focus reminders or soft blocking overlays when you reach your goals. No ads.",
            icon = Icons.Default.Layers,
            isGranted = isOverlayGranted,
            isRequired = false,
            onEnableClick = {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}")
                )
                context.startActivity(intent)
            }
        ),
        PermissionStep(
            title = "Notification Access",
            statusLabel = "Optional",
            description = "Detect distracting notifications",
            supportingNote = "Helps with focus features by identifying notifications that might pull you away.",
            icon = Icons.Default.NotificationsActive,
            isGranted = isNotificationGranted,
            isRequired = false,
            onEnableClick = {
                context.startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
            }
        )
    )

    LaunchedEffect(Unit) {
        currentStepIndex = permissionSteps.indexOfFirst { !it.isGranted }
            .takeIf { it >= 0 }
            ?: 0
    }

    // Re-check permissions when returning to the app
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isUsageGranted = checkUsageStatsPermission(context)
                isOverlayGranted = Settings.canDrawOverlays(context)
                isNotificationGranted = checkNotificationAccess(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val currentStep = permissionSteps[currentStepIndex.coerceIn(0, permissionSteps.lastIndex)]
    val isLastStep = currentStepIndex == permissionSteps.lastIndex
    val canSkipCurrentStep = !currentStep.isRequired && !currentStep.isGranted

    fun moveToNextStep() {
        if (currentStepIndex < permissionSteps.lastIndex) {
            currentStepIndex += 1
        } else {
            onContinueClick()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (canSkipCurrentStep) {
                        OutlinedButton(
                            onClick = {
                                skippedStepIndexes = skippedStepIndexes + currentStepIndex
                                moveToNextStep()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryGreen)
                        ) {
                            Text("Skip", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }

                    Button(
                        onClick = {
                            if (currentStep.isGranted) {
                                moveToNextStep()
                            } else {
                                currentStep.onEnableClick()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryGreen,
                            disabledContainerColor = Color(0xFFE2E8F0)
                        )
                    ) {
                        val buttonLabel = when {
                            currentStep.isGranted && isLastStep -> "Finish"
                            currentStep.isGranted -> "Next"
                            else -> "Enable"
                        }
                        Text(buttonLabel, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Enable permissions",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "We will guide you one step at a time so the permission flow feels smoother.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF64748B),
                lineHeight = 22.sp
            )

            PermissionStepHeader(
                currentStepIndex = currentStepIndex,
                totalSteps = permissionSteps.size,
                primaryColor = primaryGreen
            )

            PermissionCard(
                title = currentStep.title,
                statusLabel = currentStep.statusLabel,
                description = currentStep.description,
                supportingNote = currentStep.supportingNote,
                icon = currentStep.icon,
                isGranted = currentStep.isGranted,
                isRequired = currentStep.isRequired,
                primaryColor = primaryGreen
            )

            Surface(
                color = if (currentStep.isRequired) Color(0xFFF8FAFC) else Color(0xFFECFDF5),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = when {
                        currentStep.isGranted && isLastStep ->
                            "All set for this step. Tap Finish to continue into the app."
                        currentStep.isGranted ->
                            "Permission enabled. Tap Next to continue to the following step."
                        currentStep.isRequired ->
                            "This permission is required before you can finish onboarding."
                        else ->
                            "This permission is optional. You can skip it now and enable it later."
                    },
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF475569),
                    lineHeight = 20.sp
                )
            }

            if (skippedStepIndexes.isNotEmpty()) {
                Text(
                    text = "Skipped: ${
                        skippedStepIndexes
                            .sorted()
                            .joinToString(" | ") { permissionSteps[it].title }
                    }",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF64748B)
                )
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun PermissionCard(
    title: String,
    statusLabel: String,
    description: String,
    supportingNote: String,
    icon: ImageVector,
    isGranted: Boolean,
    isRequired: Boolean,
    primaryColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            if (isGranted) Color(0xFFF0FDF4) else Color(0xFFEAF5EA),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isGranted) Color(0xFF22C55E) else primaryColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = if (isRequired) Color(0xFFFEE2E2) else Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = statusLabel.uppercase(),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isRequired) Color(0xFFEF4444) else Color(0xFF64748B)
                            )
                        }
                    }
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = supportingNote,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF94A3B8),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(20.dp))
            Surface(
                color = if (isGranted) Color(0xFFF0FDF4) else Color(0xFFF8FAFC),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isGranted) {
                        Icon(Icons.Default.Check, null, tint = Color(0xFF22C55E), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = if (isGranted) "Enabled" else "Waiting for action",
                        color = if (isGranted) Color(0xFF166534) else Color(0xFF475569),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionStepHeader(
    currentStepIndex: Int,
    totalSteps: Int,
    primaryColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Step ${currentStepIndex + 1} of $totalSteps",
            style = MaterialTheme.typography.labelLarge,
            color = primaryColor,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(totalSteps) { index ->
                Surface(
                    modifier = Modifier
                        .height(8.dp)
                        .weight(1f),
                    color = if (index <= currentStepIndex) primaryColor else Color(0xFFE2E8F0),
                    shape = RoundedCornerShape(999.dp)
                ) {}
            }
        }
    }
}

private data class PermissionStep(
    val title: String,
    val statusLabel: String,
    val description: String,
    val supportingNote: String,
    val icon: ImageVector,
    val isGranted: Boolean,
    val isRequired: Boolean,
    val onEnableClick: () -> Unit
)

private fun checkUsageStatsPermission(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.packageName)
    } else {
        appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.packageName)
    }
    return mode == AppOpsManager.MODE_ALLOWED
}

private fun checkNotificationAccess(context: Context): Boolean {
    val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
    return flat != null && flat.contains(context.packageName)
}

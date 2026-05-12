package com.example.conscia.ui.tracking

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.conscia.ui.components.InstalledAppIcon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class TrackedAppInfo(
    val appName: String,
    val packageName: String,
    val isRecommended: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectTrackedAppsRoute(onContinue: (List<String>) -> Unit) {
    val context = LocalContext.current
    var installedApps by remember { mutableStateOf<List<TrackedAppInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    val selectedPackages = remember { mutableStateListOf<String>() }

    val primaryGreen = Color(0xFF006654)

    LaunchedEffect(Unit) {
        installedApps = loadInstalledApps(context)
        isLoading = false
    }

    val filteredApps = remember(installedApps, searchQuery) {
        installedApps.filter { 
            it.appName.contains(searchQuery, ignoreCase = true) || 
            it.packageName.contains(searchQuery, ignoreCase = true)
        }
    }

    val recommendedApps = remember(filteredApps) { filteredApps.filter { it.isRecommended } }
    val otherApps = remember(filteredApps) { filteredApps.filter { !it.isRecommended } }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 16.dp
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Button(
                        onClick = { onContinue(selectedPackages.toList()) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = selectedPackages.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryGreen,
                            disabledContainerColor = Color(0xFFE2E8F0)
                        )
                    ) {
                        Text(
                            text = if (selectedPackages.isEmpty()) "Select at least one app" else "Continue with ${selectedPackages.size} apps",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = primaryGreen)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        "Choose apps to track",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Select the apps you want us to monitor. We only track the apps you choose, and you can change this later.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search installed apps...") },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = primaryGreen) },
                        shape = RoundedCornerShape(16.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedIndicatorColor = primaryGreen,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                        singleLine = true
                    )
                }

                // Privacy Note
                item {
                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, null, tint = Color(0xFF64748B), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "We only monitor the apps you select. Your privacy is our priority.",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }

                // Recommended Section
                if (recommendedApps.isNotEmpty() && searchQuery.isEmpty()) {
                    item {
                        Text("Recommended", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                    }
                    items(recommendedApps, key = { it.packageName }) { app ->
                        AppRow(
                            app = app,
                            isSelected = selectedPackages.contains(app.packageName),
                            primaryColor = primaryGreen,
                            onToggle = { 
                                if (selectedPackages.contains(app.packageName)) selectedPackages.remove(app.packageName)
                                else selectedPackages.add(app.packageName)
                            }
                        )
                    }
                }

                // All Apps Section
                item {
                    Text(
                        if (searchQuery.isEmpty()) "All Apps" else "Search Results",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
                
                items(otherApps, key = { it.packageName }) { app ->
                    AppRow(
                        app = app,
                        isSelected = selectedPackages.contains(app.packageName),
                        primaryColor = primaryGreen,
                        onToggle = { 
                            if (selectedPackages.contains(app.packageName)) selectedPackages.remove(app.packageName)
                            else selectedPackages.add(app.packageName)
                        }
                    )
                }
                
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
fun AppRow(app: TrackedAppInfo, isSelected: Boolean, primaryColor: Color, onToggle: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onToggle() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFEAF5EA) else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, primaryColor) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            InstalledAppIcon(
                packageName = app.packageName,
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(10.dp))
            )
            
            Column(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                Text(app.appName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(app.packageName, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            
            if (isSelected) {
                Box(modifier = Modifier.size(24.dp).background(primaryColor, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            } else {
                Box(modifier = Modifier.size(24.dp).border(2.dp, Color(0xFFE2E8F0), CircleShape))
            }
        }
    }
}

private suspend fun loadInstalledApps(context: Context): List<TrackedAppInfo> = withContext(Dispatchers.IO) {
    val pm = context.packageManager
    val mainIntent = Intent(Intent.ACTION_MAIN, null).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
    val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
    
    val distracting = setOf(
        "com.zhiliaoapp.musically", "com.instagram.android", "com.facebook.katana",
        "com.google.android.youtube", "com.twitter.android", "com.discord"
    )

    resolveInfos.map { 
        val appInfo = it.activityInfo.applicationInfo
        TrackedAppInfo(
            appName = pm.getApplicationLabel(appInfo).toString(),
            packageName = appInfo.packageName,
            isRecommended = distracting.contains(appInfo.packageName)
        )
    }.distinctBy { it.packageName }.sortedBy { it.appName }
}

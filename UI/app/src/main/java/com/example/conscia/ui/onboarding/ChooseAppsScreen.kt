package com.example.conscia.ui.onboarding

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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.conscia.model.TrackedAppInfo
import com.example.conscia.ui.components.InstalledAppIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChooseAppsToTrackScreen(
    onSaveSelection: () -> Unit,
    onSkipSelection: () -> Unit = onSaveSelection,
    isEditingSelection: Boolean = false,
    viewModel: ChooseAppsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val primaryGreen = Color(0xFF006654)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 16.dp
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    if (uiState.selectedPackages.isNotEmpty()) {
                        Text(
                            text = "${uiState.selectedPackages.size} apps selected",
                            style = MaterialTheme.typography.labelMedium,
                            color = primaryGreen,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (isEditingSelection) {
                                    onSkipSelection()
                                } else {
                                    viewModel.skipSelection(onSkipSelection)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryGreen)
                        ) {
                            Text(
                                text = if (isEditingSelection) "Cancel" else "Skip",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Button(
                            onClick = { viewModel.saveSelection(onSaveSelection) },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            shape = RoundedCornerShape(16.dp),
                            enabled = isEditingSelection || uiState.selectedPackages.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryGreen,
                                disabledContainerColor = Color(0xFFE2E8F0)
                            )
                        ) {
                            Text(
                                text = if (isEditingSelection) "Save" else "Next",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = primaryGreen)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
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
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
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
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Recommended Section
                val recommendedApps = uiState.filteredApps.filter { it.isRecommended }
                if (recommendedApps.isNotEmpty() && uiState.searchQuery.isEmpty()) {
                    item {
                        Text("Recommended", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                    }
                    items(recommendedApps, key = { it.packageName }) { app ->
                        AppItemRow(
                            app = app,
                            isSelected = uiState.selectedPackages.contains(app.packageName),
                            primaryColor = primaryGreen,
                            onToggle = { viewModel.toggleAppSelection(app.packageName) }
                        )
                    }
                }

                // All Apps Section
                item {
                    Text(
                        if (uiState.searchQuery.isEmpty()) "All Apps" else "Search Results",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
                
                val otherApps = if (uiState.searchQuery.isEmpty()) {
                    uiState.filteredApps.filter { !it.isRecommended }
                } else {
                    uiState.filteredApps
                }

                items(otherApps, key = { it.packageName }) { app ->
                    AppItemRow(
                        app = app,
                        isSelected = uiState.selectedPackages.contains(app.packageName),
                        primaryColor = primaryGreen,
                        onToggle = { viewModel.toggleAppSelection(app.packageName) }
                    )
                }
                
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
fun AppItemRow(
    app: TrackedAppInfo,
    isSelected: Boolean,
    primaryColor: Color,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
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
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
            )

            Column(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                Text(app.appName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(app.packageName, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(primaryColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .border(2.dp, Color(0xFFE2E8F0), CircleShape)
                )
            }
        }
    }
}

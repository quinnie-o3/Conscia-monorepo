package com.example.conscia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.conscia.data.TrackedAppsDataStore
import com.example.conscia.ui.dashboard.DashboardRoute
import com.example.conscia.ui.onboarding.OnboardingRoute
import com.example.conscia.ui.onboarding.ChooseAppsToTrackScreen
import com.example.conscia.ui.permissions.PermissionsRoute
import com.example.conscia.ui.rules.RulesRoute
import com.example.conscia.ui.rules.CreateEditRuleScreen
import com.example.conscia.ui.intention.IntentionRoute
import com.example.conscia.ui.insights.InsightsRoute
import com.example.conscia.ui.settings.SettingsRoute
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ConsciaAppTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dataStore = remember { TrackedAppsDataStore(context) }
    val isOnboardingCompleted by dataStore.isOnboardingCompletedFlow.collectAsState(initial = null)
    
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    val screensWithBottomBar = listOf("dashboard", "rules", "intention", "insights", "settings")
    val showBottomBar = currentDestination?.route?.split("/")?.firstOrNull() in screensWithBottomBar

    if (isOnboardingCompleted == null) {
        Box(modifier = Modifier.fillMaxSize())
        return
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 8.dp) {
                    val items = listOf(
                        Triple("dashboard", "Home", Icons.Default.Dashboard),
                        Triple("rules", "Rules", Icons.Default.Rule),
                        Triple("intention", "Sessions", Icons.Default.Schedule),
                        Triple("insights", "Insights", Icons.Default.History),
                        Triple("settings", "Settings", Icons.Default.Settings)
                    )
                    items.forEach { (route, label, icon) ->
                        NavigationBarItem(
                            icon = { Icon(icon, null) },
                            label = { Text(label) },
                            selected = currentDestination?.hierarchy?.any { it.route == route } == true,
                            onClick = {
                                navController.navigate(route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController, 
            startDestination = if (isOnboardingCompleted == true) "dashboard" else "onboarding", 
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("onboarding") { OnboardingRoute { navController.navigate("choose_apps") } }
            composable("choose_apps") { ChooseAppsToTrackScreen({ navController.navigate("permissions") }) }
            composable("permissions") { 
                PermissionsRoute { 
                    scope.launch {
                        dataStore.setOnboardingCompleted(true)
                    }
                    navController.navigate("dashboard") { popUpTo("onboarding") { inclusive = true } } 
                } 
            }
            composable("dashboard") { DashboardRoute() }
            composable("rules") { 
                RulesRoute(
                    onBackClick = { navController.popBackStack() }, 
                    onCreateRuleClick = { navController.navigate("create_rule/-1") }, 
                    onEditRuleClick = { id -> navController.navigate("create_rule/$id") }
                ) 
            }
            composable(
                route = "create_rule/{ruleId}",
                arguments = listOf(navArgument("ruleId") { type = NavType.LongType })
            ) { backStack ->
                val ruleId = backStack.arguments?.getLong("ruleId")
                CreateEditRuleScreen(
                    ruleId = ruleId, 
                    onBackClick = { navController.popBackStack() }, 
                    onSelectAppClick = {}
                )
            }
            composable("intention") { IntentionRoute(appName = "App", onBackClick = { navController.popBackStack() }, onContinueClick = {}) }
            composable("insights") { InsightsRoute() }
            composable("settings") { SettingsRoute(onBackClick = { navController.popBackStack() }, isDarkMode = false, onDarkModeChange = {}, onNavigateToSection = {}) }
        }
    }
}

@Composable
fun ConsciaAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = lightColorScheme(primary = Color(0xFF006654)), content = content)
}

package com.example.conscia

import android.os.Bundle
import androidx.activity.ComponentActivity
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Rule
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.conscia.data.TrackedAppsDataStore
import com.example.conscia.ui.dashboard.DashboardRoute
import com.example.conscia.ui.onboarding.OnboardingRoute
import com.example.conscia.ui.onboarding.ChooseAppsToTrackScreen
import com.example.conscia.ui.onboarding.StarterRulesRoute
import com.example.conscia.ui.permissions.PermissionsRoute
import com.example.conscia.ui.rules.CreateEditRuleScreen
import com.example.conscia.ui.rules.SelectRuleAppScreen
import com.example.conscia.ui.tracked.TrackedAppsRoute
import com.example.conscia.ui.intention.SessionHistoryScreen
import com.example.conscia.ui.insights.InsightsRoute
import com.example.conscia.ui.settings.SettingsRoute
import com.example.conscia.ui.settings.ManageIntentionsScreen
import com.example.conscia.ui.settings.UserInformationScreen
import com.example.conscia.ui.auth.LoginScreen
import com.example.conscia.ui.auth.RegisterScreen
import com.example.conscia.data.rule.RuleRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var dataStore: TrackedAppsDataStore
    
    @Inject
    lateinit var ruleRepository: RuleRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val isDarkModePref by dataStore.isDarkModeFlow.collectAsState(initial = isSystemInDarkTheme())
            
            ConsciaAppTheme(darkTheme = isDarkModePref) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNavigation(dataStore, ruleRepository)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(dataStore: TrackedAppsDataStore, ruleRepository: RuleRepository) {
    val scope = rememberCoroutineScope()
    val appPreferences by dataStore.appPreferencesFlow.collectAsState(initial = null)
    val isDarkMode by dataStore.isDarkModeFlow.collectAsState(initial = isSystemInDarkTheme())
    
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    val screensWithBottomBar = listOf("dashboard", "rules", "insights", "settings")
    val showBottomBar = currentDestination?.route?.split("/")?.firstOrNull() in screensWithBottomBar

    if (appPreferences == null) {
        Box(modifier = Modifier.fillMaxSize())
        return
    }

    val accessToken = appPreferences?.accessToken
    val isOnboardingCompleted = appPreferences?.isOnboardingCompleted == true
    val routeAfterAuth = if (isOnboardingCompleted) "dashboard" else "choose_apps"

    LaunchedEffect(accessToken, currentDestination?.route) {
        val route = currentDestination?.route
        if (accessToken == null && route != null && route != "login" && route != "register") {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 8.dp) {
                    val items = listOf(
                        Triple("dashboard", "Home", Icons.Default.Dashboard),
                        Triple("rules", "Rules", Icons.AutoMirrored.Filled.Rule),
                        Triple("insights", "Insights", Icons.Default.Analytics),
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
            startDestination = if (accessToken == null) "login" else routeAfterAuth,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn(animationSpec = tween(300)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) },
            popEnterTransition = { fadeIn(animationSpec = tween(300)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) },
            popExitTransition = { fadeOut(animationSpec = tween(300)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) }
        ) {
            // --- AUTH ---
            composable("login") { 
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(routeAfterAuth) {
                            popUpTo("login") { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onNavigateToRegister = { navController.navigate("register") },
                    onBackClick = {
                        Toast.makeText(
                            navController.context,
                            "Please sign in or create an account to continue.",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                ) 
            }
            composable("register") {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate(routeAfterAuth) {
                            popUpTo("register") { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onNavigateToLogin = { navController.popBackStack() }
                )
            }

            // --- ONBOARDING ---
            composable("onboarding") { OnboardingRoute { navController.navigate("choose_apps") } }
            composable("choose_apps") { 
                ChooseAppsToTrackScreen(onSaveSelection = { navController.navigate("starter_rules") }) 
            }
            composable("starter_rules") {
                StarterRulesRoute(
                    onBackClick = { navController.popBackStack() },
                    onContinueClick = { navController.navigate("permissions") }
                )
            }
            composable("permissions") { 
                PermissionsRoute { 
                    scope.launch { dataStore.setOnboardingCompleted(true) }
                    navController.navigate("dashboard") { popUpTo("onboarding") { inclusive = true } } 
                } 
            }

            // --- MAIN ---
            composable("dashboard") { 
                DashboardRoute(
                    onNavigateToSettings = { navController.navigate("settings") },
                    onNewGoalClick = { navController.navigate("create_rule/-1") }
                )
            }
            composable("rules") { 
                TrackedAppsRoute(
                    onBackClick = { navController.popBackStack() }, 
                    onAddClick = { navController.navigate("create_rule/-1") },
                    onAppClick = { id -> navController.navigate("create_rule/$id") }
                ) 
            }
            composable("create_rule/{ruleId}", arguments = listOf(navArgument("ruleId") { type = NavType.LongType })) { backStack ->
                val returnToRules = {
                    if (!navController.popBackStack("rules", inclusive = false)) {
                        navController.navigate("rules") {
                            popUpTo("dashboard") { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                }
                val selectedPackageName by backStack.savedStateHandle
                    .getStateFlow("selectedAppPackageName", "")
                    .collectAsState()
                val selectedAppName by backStack.savedStateHandle
                    .getStateFlow("selectedAppName", "")
                    .collectAsState()
                CreateEditRuleScreen(
                    ruleId = backStack.arguments?.getLong("ruleId"), 
                    onBackClick = returnToRules,
                    onSelectAppClick = { navController.navigate("select_rule_app") },
                    selectedAppPackageName = selectedPackageName,
                    selectedAppName = selectedAppName,
                    onSelectedAppConsumed = {
                        backStack.savedStateHandle["selectedAppPackageName"] = ""
                        backStack.savedStateHandle["selectedAppName"] = ""
                    }
                )
            }
            composable("select_rule_app") {
                SelectRuleAppScreen(
                    onBackClick = { navController.popBackStack() },
                    onAppSelected = { packageName, appName ->
                        navController.previousBackStackEntry?.savedStateHandle?.set("selectedAppPackageName", packageName)
                        navController.previousBackStackEntry?.savedStateHandle?.set("selectedAppName", appName)
                        navController.popBackStack()
                    }
                )
            }
            composable("sessions") {
                SessionHistoryScreen(onBackClick = { navController.popBackStack() })
            }
            composable("insights") { InsightsRoute() }
            
            // --- SETTINGS ---
            composable("settings") { 
                SettingsRoute(
                    onBackClick = { navController.popBackStack() }, 
                    isDarkMode = isDarkMode, 
                    onDarkModeChange = { enabled ->
                        scope.launch { dataStore.setDarkModeEnabled(enabled) }
                    }, 
                    onNavigateToSection = { section ->
                        when (section) {
                            "manage_intentions" -> navController.navigate("manage_intentions")
                            "user_info" -> navController.navigate("user_information")
                            "logout" -> {
                                scope.launch {
                                    ruleRepository.deleteAllLocalRules()
                                    dataStore.clearAuth()
                                    navController.navigate("login") { popUpTo(0) { inclusive = true } }
                                }
                            }
                        }
                    }
                ) 
            }
            composable("manage_intentions") {
                ManageIntentionsScreen(onBackClick = { navController.popBackStack() })
            }
            composable("user_information") {
                UserInformationScreen(onBackClick = { navController.popBackStack() })
            }
        }
    }
}

@Composable
fun ConsciaAppTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(primary = Color(0xFF00A38D), secondary = Color(0xFF006654))
    } else {
        lightColorScheme(primary = Color(0xFF006654), secondary = Color(0xFF00A38D))
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}

package com.londontubeai.navigator.navigation

import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.londontubeai.navigator.data.network.ConnectionState
import com.londontubeai.navigator.data.network.NetworkMonitor
import com.londontubeai.navigator.data.preferences.AppPreferences
import com.londontubeai.navigator.ui.components.BottomNavBar
import com.londontubeai.navigator.ui.components.OfflineBanner
import com.londontubeai.navigator.ui.components.OnlineRestoredBanner
import com.londontubeai.navigator.ui.screens.home.HomeScreen
import com.londontubeai.navigator.ui.screens.map.MapScreen
import com.londontubeai.navigator.ui.screens.nearby.NearbyDetailScreen
import com.londontubeai.navigator.ui.screens.onboarding.OnboardingScreen
import com.londontubeai.navigator.ui.screens.premium.PremiumScreen
import com.londontubeai.navigator.ui.screens.privacy.PrivacyScreen
import com.londontubeai.navigator.ui.screens.terms.TermsScreen
import com.londontubeai.navigator.ui.screens.licenses.LicensesScreen
import com.londontubeai.navigator.ui.screens.route.RouteScreen
import com.londontubeai.navigator.ui.screens.settings.SettingsScreen
import com.londontubeai.navigator.ui.screens.station.StationDetailScreen
import com.londontubeai.navigator.ui.screens.station.StationListScreen
import com.londontubeai.navigator.ui.screens.splash.AnimatedSplashScreen
import com.londontubeai.navigator.ui.screens.status.StatusScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(
    networkMonitor: NetworkMonitor? = null,
    startOnboarding: Boolean = false,
    appPreferences: AppPreferences? = null,
    iconGradientStart: Color = Color(0xFF0A1628),
    iconGradientEnd: Color = Color(0xFF003688),
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val scope = rememberCoroutineScope()

    val showBottomBar = currentRoute in Screen.bottomNavItems.map { it.route }

    // Set status bar icon color: white icons on dark-header screens, dark icons on light-header screens
    val darkHeaderRoutes = remember {
        setOf("home", "status", "stations", "settings", "nearby_detail")
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val useLightIcons = currentRoute != null &&
                (darkHeaderRoutes.any { currentRoute == it } ||
                 currentRoute.orEmpty().startsWith("station/"))
            WindowInsetsControllerCompat(window, view).isAppearanceLightStatusBars = !useLightIcons
        }
    }

    // Observe connectivity
    var isOffline by remember { mutableStateOf(false) }
    var showReconnected by remember { mutableStateOf(false) }

    LaunchedEffect(networkMonitor) {
        networkMonitor?.connectionState?.collect { state ->
            val wasOffline = isOffline
            isOffline = state == ConnectionState.OFFLINE
            if (wasOffline && !isOffline) {
                showReconnected = true
                delay(3000)
                showReconnected = false
            }
        }
    }

    val startDest = Screen.Splash.route
    val afterSplashDest = if (startOnboarding) Screen.Onboarding.route else Screen.Home.route

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(navController = navController)
            }
        },
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            OfflineBanner(isOffline = isOffline)
            OnlineRestoredBanner(justReconnected = showReconnected)
            NavHost(
                navController = navController,
                startDestination = startDest,
                modifier = Modifier.weight(1f),
            ) {
                composable(Screen.Splash.route) {
                    AnimatedSplashScreen(
                        onSplashComplete = {
                            navController.navigate(afterSplashDest) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        },
                        iconGradientStart = iconGradientStart,
                        iconGradientEnd = iconGradientEnd,
                    )
                }

                composable(Screen.Onboarding.route) {
                    OnboardingScreen(
                        onComplete = {
                            scope.launch {
                                appPreferences?.setOnboardingComplete(true)
                            }
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Onboarding.route) { inclusive = true }
                            }
                        },
                    )
                }

                composable(Screen.Home.route) {
                    HomeScreen(
                        onNavigateToRoute = {
                            navController.navigate(Screen.Route.route) {
                                popUpTo(Screen.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onNavigateToStatus = {
                            navController.navigate(Screen.Status.route) {
                                popUpTo(Screen.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onNavigateToStations = {
                            navController.navigate(Screen.Stations.route) {
                                popUpTo(Screen.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onNavigateToMap = {
                            navController.navigate(Screen.TubeMap.route)
                        },
                        onNavigateToNearby = {
                            navController.navigate(Screen.NearbyDetail.route)
                        },
                    )
                }

                composable(Screen.Route.route) {
                    RouteScreen(
                        onNavigateToMap = { fromId, toId ->
                            navController.navigate(Screen.TubeMap.createRoute(fromId, toId))
                        },
                    )
                }

                composable(Screen.Status.route) {
                    StatusScreen()
                }

                composable(Screen.Stations.route) {
                    StationListScreen(
                        onStationClick = { stationId ->
                            navController.navigate(Screen.StationDetail.createRoute(stationId))
                        },
                    )
                }

                composable(Screen.Settings.route) {
                    SettingsScreen(
                        onNavigateToPremium = {
                            navController.navigate(Screen.Premium.route)
                        },
                        onNavigateToPrivacy = {
                            navController.navigate(Screen.Privacy.route)
                        },
                        onNavigateToTerms = {
                            navController.navigate(Screen.Terms.route)
                        },
                        onNavigateToLicenses = {
                            navController.navigate(Screen.Licenses.route)
                        },
                    )
                }

                composable(Screen.Premium.route) {
                    PremiumScreen(
                        onBack = { navController.popBackStack() },
                    )
                }

                composable(Screen.Privacy.route) {
                    PrivacyScreen(
                        onBack = { navController.popBackStack() },
                    )
                }

                composable(Screen.Terms.route) {
                    TermsScreen(
                        onBack = { navController.popBackStack() },
                    )
                }

                composable(Screen.Licenses.route) {
                    LicensesScreen(
                        onBack = { navController.popBackStack() },
                    )
                }

                composable(Screen.NearbyDetail.route) {
                    NearbyDetailScreen(
                        onBack = { navController.popBackStack() },
                        onStationClick = { stationId ->
                            navController.navigate(Screen.StationDetail.createRoute(stationId))
                        },
                    )
                }

                composable(
                    route = "map?fromId={fromId}&toId={toId}",
                    arguments = listOf(
                        navArgument("fromId") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        },
                        navArgument("toId") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        },
                    ),
                ) { backStackEntry ->
                    val fromId = backStackEntry.arguments?.getString("fromId")
                    val toId = backStackEntry.arguments?.getString("toId")
                    MapScreen(
                        onBack = { navController.popBackStack() },
                        onStationClick = { stationId ->
                            navController.navigate(Screen.StationDetail.createRoute(stationId))
                        },
                        routeFromId = fromId,
                        routeToId = toId,
                    )
                }

                composable(
                    route = Screen.StationDetail.route,
                    arguments = listOf(
                        navArgument("stationId") { type = NavType.StringType },
                    ),
                ) { backStackEntry ->
                    val stationId = backStackEntry.arguments?.getString("stationId") ?: return@composable
                    StationDetailScreen(
                        stationId = stationId,
                        onBack = { navController.popBackStack() },
                        onStationClick = { id ->
                            navController.navigate(Screen.StationDetail.createRoute(id))
                        },
                    )
                }
            }
        }
    }
}

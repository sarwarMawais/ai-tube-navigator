package com.londontubeai.navigator.navigation

import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsSubway
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Traffic
import androidx.compose.material.icons.outlined.DirectionsSubway
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Traffic
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector? = null,
    val unselectedIcon: ImageVector? = null,
) {
    data object Home : Screen(
        route = "home",
        title = "Home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
    )

    data object Route : Screen(
        route = "route",
        title = "Route",
        selectedIcon = Icons.Filled.Explore,
        unselectedIcon = Icons.Outlined.Explore,
    ) {
        fun createRoute(
            toId: String? = null,
            toName: String? = null,
            toLat: Double? = null,
            toLng: Double? = null,
        ): String {
            val params = buildList {
                if (!toId.isNullOrBlank()) add("toId=${Uri.encode(toId)}")
                if (!toName.isNullOrBlank()) add("toName=${Uri.encode(toName)}")
                if (toLat != null) add("toLat=$toLat")
                if (toLng != null) add("toLng=$toLng")
            }
            return if (params.isEmpty()) route else "$route?${params.joinToString("&")}" 
        }
    }

    data object Status : Screen(
        route = "status",
        title = "Status",
        selectedIcon = Icons.Filled.Traffic,
        unselectedIcon = Icons.Outlined.Traffic,
    )

    data object Stations : Screen(
        route = "stations",
        title = "Stations",
        selectedIcon = Icons.Filled.DirectionsSubway,
        unselectedIcon = Icons.Outlined.DirectionsSubway,
    )

    data object Settings : Screen(
        route = "settings",
        title = "Settings",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
    )

    data object Splash : Screen(
        route = "splash",
        title = "Splash",
    )

    data object Onboarding : Screen(
        route = "onboarding",
        title = "Welcome",
    )

    data object Premium : Screen(
        route = "premium",
        title = "Premium",
    )

    data object Privacy : Screen(
        route = "privacy",
        title = "Privacy",
    )

    data object Terms : Screen(
        route = "terms",
        title = "Terms of Service",
    )

    data object Licenses : Screen(
        route = "licenses",
        title = "Open-Source Licenses",
    )

    data object TubeMap : Screen(
        route = "map",
        title = "Map",
    ) {
        fun createRoute(fromId: String? = null, toId: String? = null): String {
            return if (!fromId.isNullOrBlank() && !toId.isNullOrBlank()) {
                "map?fromId=$fromId&toId=$toId"
            } else {
                route
            }
        }
    }

    data object NearbyDetail : Screen(
        route = "nearby_detail",
        title = "Nearby Stations",
    )

    data object StationDetail : Screen(
        route = "station/{stationId}",
        title = "Station Detail",
    ) {
        fun createRoute(stationId: String) = "station/$stationId"
    }

    data object RouteResult : Screen(
        route = "route_result/{fromId}/{toId}",
        title = "Route Result",
    ) {
        fun createRoute(fromId: String, toId: String) = "route_result/$fromId/$toId"
    }

    companion object {
        val bottomNavItems = listOf(Home, Route, Status, Stations, Settings)
    }
}

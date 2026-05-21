package com.engineerstech.weathersnap.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.engineerstech.weathersnap.ui.home.HomeScreen
import com.engineerstech.weathersnap.ui.report.CreateReportScreen
import kotlinx.serialization.Serializable

val LocalNavigationProvider = staticCompositionLocalOf<NavHostController> {
    error("No NavHost provided")
}

@Composable
fun NavGraph(
    modifier: Modifier,
    startDestination: Routes
) {
    val navHostController = LocalNavigationProvider.current

    NavHost(
        navController = navHostController,
        startDestination = startDestination,
        modifier = modifier
    ) {

        composable<Routes.Home> {
            HomeScreen()
        }

        composable<Routes.CreateReport> { backStackEntry->
            val cityName = backStackEntry.arguments?.getString("cityName")
            val lat = backStackEntry.arguments?.getDouble("lat")
            val long = backStackEntry.arguments?.getDouble("long")
            CreateReportScreen(cityName = cityName, lat = lat, long = long)
        }

    }
}

@Serializable
sealed class Routes {
    @Serializable
    data object Home : Routes()

    @Serializable
    data class CreateReport(
        val cityName: String,
        val lat: Double,
        val long: Double,
    ) : Routes()
}
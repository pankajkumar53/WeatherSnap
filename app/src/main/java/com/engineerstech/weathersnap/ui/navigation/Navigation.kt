package com.engineerstech.weathersnap.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.engineerstech.weathersnap.ui.home.HomeScreen
import com.engineerstech.weathersnap.ui.report.CreateReportScreen
import com.engineerstech.weathersnap.ui.report.SavedReportScreen
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

        composable<Routes.CreateReport> {
            CreateReportScreen()
        }

        composable<Routes.SavedReports> {
            SavedReportScreen()
        }

    }
}

@Serializable
sealed class Routes {
    @Serializable
    data object Home : Routes()

    @Serializable
    data object SavedReports : Routes()

    @Serializable
    data class CreateReport(
        val cityName: String,
        val lat: Double,
        val long: Double,
    ) : Routes()
}
package com.engineerstech.weathersnap.ui.report

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.isPopupLayout
import androidx.hilt.navigation.compose.hiltViewModel
import com.engineerstech.weathersnap.data.api.ApiResult
import com.engineerstech.weathersnap.ui.component.CustomColumn
import com.engineerstech.weathersnap.ui.component.ErrorView
import com.engineerstech.weathersnap.ui.component.HeaderCard
import com.engineerstech.weathersnap.ui.component.LoadingView
import com.engineerstech.weathersnap.ui.home.HomeViewModel
import com.engineerstech.weathersnap.ui.home.WeatherCard
import com.engineerstech.weathersnap.ui.navigation.LocalNavigationProvider

@Composable
fun CreateReportScreen(cityName: String?, lat: Double?, long: Double?) {

    val homeViewModel: HomeViewModel = hiltViewModel()
    val weatherData by homeViewModel.weatherData.collectAsState()

    val navController = LocalNavigationProvider.current

    LaunchedEffect(Unit) {
        if (lat != null && long != null)  {
            homeViewModel.getWeather( lat, long)
        }
    }

    CustomColumn {

        HeaderCard(
            title = "Create Report",
            subTitle = "Capture, compress, annotate",
            buttonTitle = "Back"
        ) { navController.popBackStack() }

        Spacer(modifier = Modifier.height(16.dp))

        AnimatedContent(
            targetState = weatherData,
            transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(300)) },
            label = "WeatherDataAnimation"
        ) { weatherData ->
            when (weatherData) {
                is ApiResult.Loading -> {
                    LoadingView(message = "Fetching conditions...")
                }

                is ApiResult.Error -> {
                    ErrorView(message = weatherData.message ?: "Unable to fetch weather info.")
                }

                is ApiResult.Success -> {
                    val weather = weatherData.data
                    if (weather != null) {
                        WeatherCard(
                            weather = weather,
                            cityName = cityName ?: "Unknown Location",
                            buttonDisabled = false,
                            onCreateReport = { }
                        )
                    }
                }

                else -> {
                    /* Enjoy 😎😎😎😎 */
                }
            }
        }



    }

}
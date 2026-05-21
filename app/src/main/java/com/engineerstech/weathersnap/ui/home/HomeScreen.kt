package com.engineerstech.weathersnap.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.engineerstech.weathersnap.data.api.ApiResult
import com.engineerstech.weathersnap.domain.models.WeatherResponse
import com.engineerstech.weathersnap.ui.component.CustomColumn
import com.engineerstech.weathersnap.ui.component.ErrorView
import com.engineerstech.weathersnap.ui.component.HeaderCard
import com.engineerstech.weathersnap.ui.component.LoadingView
import com.engineerstech.weathersnap.ui.component.SearchField
import com.engineerstech.weathersnap.ui.navigation.LocalNavigationProvider
import com.engineerstech.weathersnap.ui.navigation.Routes
import com.engineerstech.weathersnap.ui.theme.AppGray
import com.engineerstech.weathersnap.ui.theme.BackGroundColor
import com.engineerstech.weathersnap.ui.theme.BackgroundBottomColor
import com.engineerstech.weathersnap.ui.theme.BackgroundTopColor
import com.engineerstech.weathersnap.ui.theme.DarkYellow
import com.engineerstech.weathersnap.ui.theme.GreenColor
import com.engineerstech.weathersnap.ui.theme.LightYellow
import com.engineerstech.weathersnap.ui.theme.SkyBlueColor
import com.engineerstech.weathersnap.ui.theme.TempColor

@Composable
fun HomeScreen() {
    val appViewModel: HomeViewModel = hiltViewModel()
    val searchData by appViewModel.searchData.collectAsState()
    val searchQuery by appViewModel.searchQuery.collectAsState()
    val selected = appViewModel.selectedCity.collectAsState().value
    val weatherData by appViewModel.weatherData.collectAsState()
    val navController = LocalNavigationProvider.current

    CustomColumn {
        /* Banner Card */
        HeaderCard(
            title = "WeatherSnap",
            subTitle = "Live weather report with camera evidence",
            buttonTitle = "Reports"
        ) { }

        Spacer(modifier = Modifier.height(16.dp))

        /* Search Card */
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.Black)
        ) {
            SearchField(
                searchQuery = searchQuery,
                onValueChange = { appViewModel.updateSearchQuery(it) },
                onClick = {
                    selected?.let {
                        appViewModel.getWeather(it.latitude, it.longitude)
                    }
                }
            )
        }

        /* Search Data Section with smooth transitions */
        AnimatedContent(
            targetState = searchData,
            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(250)) },
            label = "SearchDataAnimation"
        ) { targetSearchData ->
            when (targetSearchData) {
                is ApiResult.Loading -> {
                    LoadingView(modifier = Modifier.height(150.dp), message = "Searching cities...")
                }

                is ApiResult.Error -> {
                    ErrorView(
                        message = targetSearchData.message ?: "Failed to find locations.",
                        modifier = Modifier.height(150.dp)
                    )
                }

                is ApiResult.Success -> {
                    val results = targetSearchData.data?.results
                    if (results.isNullOrEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "No cities found", color = Color.White)
                        }
                    } else {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .padding(top = 12.dp),
                            colors = CardDefaults.cardColors(containerColor = AppGray),
                        ) {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                items(results) { city ->
                                    OutlinedButton(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        onClick = { appViewModel.selectCity(city) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (selected?.name === city.name) Color.White.copy(
                                                .1f
                                            ) else Color.Transparent
                                        )
                                    ) {
                                        Text(
                                            text = "${city.name}, ${city.country}",
                                            color = Color.White,
                                            fontSize = 14.sp
                                        )
                                    }
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        thickness = 1.dp,
                                        color = Color.White.copy(alpha = 0.1f)
                                    )
                                }
                            }
                        }
                    }
                }

                else -> {
                    Spacer(modifier = Modifier.height(0.dp))
                }
            }
        }

        /* Weather Detail Section with smooth transitions */
        AnimatedContent(
            targetState = weatherData,
            transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(300)) },
            label = "WeatherDataAnimation"
        ) { targetWeatherData ->
            when (targetWeatherData) {
                is ApiResult.Loading -> {
                    LoadingView(
                        modifier = Modifier.padding(top = 16.dp),
                        message = "Fetching conditions..."
                    )
                }

                is ApiResult.Error -> {
                    ErrorView(
                        message = targetWeatherData.message ?: "Unable to fetch weather info.",
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

                is ApiResult.Success -> {
                    val weather = targetWeatherData.data
                    if (weather != null) {
                        WeatherCard(
                            weather = weather,
                            cityName = "${selected?.name ?: "Unknown Location"}, ${selected?.country ?: ""}",
                            onCreateReport = {
                                navController.navigate(
                                    Routes.CreateReport(
                                        cityName = "${selected?.name ?: "Unknown Location"}, ${selected?.country ?: ""}",
                                        lat = selected?.latitude ?: 0.0,
                                        long = selected?.longitude ?: 0.0
                                    )
                                )
                            }
                        )
                    }
                }

                else -> {
                    Spacer(modifier = Modifier.height(0.dp))
                }
            }
        }
    }
}


@Composable
fun WeatherCard(
    weather: WeatherResponse,
    cityName: String,
    buttonDisabled: Boolean = true,
    onCreateReport: () -> Unit
) {
    val condition = getWeatherCondition(weather.current.weather_code)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        colors = CardDefaults.cardColors(containerColor = AppGray),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // City + Temp Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = cityName,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = condition,
                        color = Color.LightGray,
                        fontSize = 14.sp
                    )
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = TempColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${weather.current.temperature_2m.toInt()}°C",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatChip(
                    label = "Humidity",
                    value = "${weather.current.relative_humidity_2m}${weather.current_units.relative_humidity_2m}",
                    valueColor = GreenColor,
                    modifier = Modifier.weight(1f)
                )
                StatChip(
                    label = "Wind",
                    value = "${weather.current.wind_speed_10m} ${weather.current_units.wind_speed_10m}",
                    valueColor = SkyBlueColor,
                    modifier = Modifier.weight(1f)
                )
                StatChip(
                    label = "Pressure",
                    value = "${weather.current.pressure_msl.toInt()} hPa",
                    valueColor = LightYellow,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (buttonDisabled) {
                // Report Readiness Row
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Report readiness",
                            color = Color.LightGray,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Camera and Room DB enabled",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Create Report Button
                Button(
                    onClick = onCreateReport,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkYellow),
                    shape = RoundedCornerShape(50.dp)
                ) {
                    Text(
                        text = "Create Report",
                        color = Color.Black,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

        }
    }
}

@Composable
fun StatChip(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)
        ) {
            Text(
                text = label,
                color = Color.LightGray,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = valueColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

fun getWeatherCondition(code: Int): String {
    return when (code) {
        0 -> "Clear sky"
        1, 2, 3 -> "Partly cloudy"
        45, 48 -> "Foggy"
        51, 53, 55 -> "Drizzle"
        61, 63, 65 -> "Rainy"
        71, 73, 75 -> "Snowy"
        80, 81, 82 -> "Rain showers"
        95 -> "Thunderstorm"
        96, 99 -> "Thunderstorm with hail"
        else -> "Unknown"
    }
}
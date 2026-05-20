package com.engineerstech.weathersnap.ui.home

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradient
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.engineerstech.weathersnap.data.api.ApiResult
import com.engineerstech.weathersnap.domain.models.SearchResults
import com.engineerstech.weathersnap.ui.component.HeaderCard
import com.engineerstech.weathersnap.ui.component.SearchField
import com.engineerstech.weathersnap.ui.theme.AppGray
import com.engineerstech.weathersnap.ui.theme.BackGroundColor
import com.engineerstech.weathersnap.ui.theme.BackgroundBottomColor
import com.engineerstech.weathersnap.ui.theme.BackgroundTopColor
import com.engineerstech.weathersnap.ui.theme.BlackColor
import com.engineerstech.weathersnap.ui.theme.DarkYellow
import com.engineerstech.weathersnap.ui.theme.SkyBlue


@Composable
fun HomeScreen() {

    val appViewModel: HomeViewModel = hiltViewModel()
    val searchData by appViewModel.searchData.collectAsState()
    val searchQuery by appViewModel.searchQuery.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        BackgroundTopColor,
                        BackgroundBottomColor, BackGroundColor
                    )
                )
            )
            .padding(8.dp)
    ) {

        /* Banner Card */
        HeaderCard(
            title = "WeatherSnap",
            subTitle = "Live weather report with camera evidence",
            buttonTitle = "Reports"
        ) { }

        Spacer(modifier = Modifier.height(16.dp))

        /* Search Card */
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black
            )
        ) {
            SearchField(
                searchQuery = searchQuery,
                onValueChange = {
                    appViewModel.updateSearchQuery(it)
                },
                onClick = {
                    appViewModel.searchCities(searchQuery)
                }
            )
        }

        when (searchData) {
            is ApiResult.Loading -> {
                Text("Loading...")
            }

            is ApiResult.Error -> {
                (searchData as ApiResult.Error).message?.let {
                    Text(text = it)
                }
            }

            is ApiResult.Success -> {

                val results =
                    (searchData as ApiResult.Success<SearchResults>).data?.results

                if (results.isNullOrEmpty()) {

                    Text(
                        text = "No cities found",
                        color = Color.White,
                        modifier = Modifier.padding(16.dp)
                    )

                } else {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
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
                                        .padding(8.dp),
                                    onClick = {

                                    } ) {
                                    Text(
                                        text = "${city.name}, ${city.country}",
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                }

                                HorizontalDivider(thickness = 1.3.dp)
                            }
                        }
                    }
                }
            }

            else -> {
                /* Enjoy Your Error 😎😎😎 */
            }
        }
    }
}

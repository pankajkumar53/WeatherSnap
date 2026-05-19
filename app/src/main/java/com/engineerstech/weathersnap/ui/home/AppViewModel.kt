package com.engineerstech.weathersnap.ui.home

import androidx.lifecycle.ViewModel
import com.engineerstech.weathersnap.data.api.ApiResult
import com.engineerstech.weathersnap.data.repo.AppRepo
import com.engineerstech.weathersnap.domain.models.SearchResults
import com.engineerstech.weathersnap.domain.models.WeatherResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val appRepo: AppRepo
): ViewModel() {

    private val _searchData = MutableStateFlow<ApiResult<SearchResults>>(ApiResult.Loading())
    val searchData: StateFlow<ApiResult<SearchResults>> = _searchData

    private val _weatherData = MutableStateFlow<ApiResult<WeatherResponse>>(ApiResult.Loading())
    val weatherData: StateFlow<ApiResult<WeatherResponse>> = _weatherData


    suspend fun searchCities(name: String) {
        _searchData.value = appRepo.searchCities(name)
    }

    suspend fun getWeather(latitude: Double, longitude: Double) {
        _weatherData.value = appRepo.getWeather(latitude, longitude)
    }


}
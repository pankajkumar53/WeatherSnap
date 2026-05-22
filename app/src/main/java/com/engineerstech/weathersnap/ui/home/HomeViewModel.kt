package com.engineerstech.weathersnap.ui.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.engineerstech.weathersnap.data.api.ApiResult
import com.engineerstech.weathersnap.data.repo.AppRepo
import com.engineerstech.weathersnap.domain.models.Result
import com.engineerstech.weathersnap.domain.models.SearchResults
import com.engineerstech.weathersnap.domain.models.WeatherResponse
import com.engineerstech.weathersnap.util.DraftState
import com.engineerstech.weathersnap.util.SavedStateKeys.LAST_LAT
import com.engineerstech.weathersnap.util.SavedStateKeys.LAST_LONG
import com.engineerstech.weathersnap.util.SavedStateKeys.SEARCH_QUERY
import com.engineerstech.weathersnap.util.SavedStateKeys.SELECTED_CITY
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Collections
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val appRepo: AppRepo,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val searchQuery = DraftState(savedStateHandle, SEARCH_QUERY, "")
    val selectedCity = DraftState<Result?>(savedStateHandle, SELECTED_CITY, null)

    private val _weatherData = MutableStateFlow<ApiResult<WeatherResponse>>(ApiResult.Idle())
    val weatherData: StateFlow<ApiResult<WeatherResponse>> = _weatherData.asStateFlow()

    private val _apiTrigger = MutableStateFlow("")
    private var _isManuallySelected = false

    private val searchCache = Collections.synchronizedMap(linkedMapOf<String, SearchResults>())
    private val MAX_CACHE_SIZE = 20

    init {
        val lat = savedStateHandle.get<Double>(LAST_LAT)
        val lng = savedStateHandle.get<Double>(LAST_LONG)
        if (lat != null && lng != null) {
            getWeather(lat, lng)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val searchData: StateFlow<ApiResult<SearchResults>> = _apiTrigger
        .debounce(500)
        .distinctUntilChanged()
        .flatMapLatest { rawQuery ->
            flow {
                val query = rawQuery.trim().lowercase()
                if (query.length <= 2) { emit(ApiResult.Idle()); return@flow }
                searchCache[query]?.let { emit(ApiResult.Success(it)); return@flow }
                emit(ApiResult.Loading())
                try {
                    val result = appRepo.searchCities(query)
                    if (result is ApiResult.Success && result.data != null) {
                        synchronized(searchCache) {
                            if (searchCache.size >= MAX_CACHE_SIZE)
                                searchCache.remove(searchCache.keys.firstOrNull())
                            searchCache[query] = result.data
                        }
                    }
                    emit(result)
                } catch (e: Exception) {
                    emit(ApiResult.Error(e.message ?: "Unknown Network Error"))
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ApiResult.Idle())

    fun updateSearchQuery(query: String) {
        searchQuery.update(query)
        if (!_isManuallySelected) _apiTrigger.value = query
        else _isManuallySelected = false
    }

    fun selectCity(city: Result) {
        _isManuallySelected = true
        selectedCity.update(city)
        searchQuery.update("${city.name}, ${city.country}")
    }

    fun getWeather(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _weatherData.value = ApiResult.Loading()
            val result = appRepo.getWeather(latitude, longitude)
            _weatherData.value = result

            if (result is ApiResult.Success) {
                savedStateHandle[LAST_LAT] = latitude
                savedStateHandle[LAST_LONG] = longitude
            }
            _apiTrigger.value = ""
        }
    }
}
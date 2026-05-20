package com.engineerstech.weathersnap.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.engineerstech.weathersnap.data.api.ApiResult
import com.engineerstech.weathersnap.data.repo.AppRepo
import com.engineerstech.weathersnap.domain.models.SearchResults
import com.engineerstech.weathersnap.domain.models.WeatherResponse
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
    private val appRepo: AppRepo
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val searchCache = Collections.synchronizedMap(linkedMapOf<String, SearchResults>())
    private val MAX_CACHE_SIZE = 20

    private val _weatherData = MutableStateFlow<ApiResult<WeatherResponse>>(ApiResult.Idle())
    val weatherData: StateFlow<ApiResult<WeatherResponse>> = _weatherData.asStateFlow()

    private val _manualSearchData = MutableStateFlow<ApiResult<SearchResults>>(ApiResult.Idle())
    val manualSearchData: StateFlow<ApiResult<SearchResults>> = _manualSearchData.asStateFlow()


    // Search
    @OptIn(ExperimentalCoroutinesApi::class)
    val searchData: StateFlow<ApiResult<SearchResults>> = _searchQuery
        .debounce(500)
        .distinctUntilChanged()
        .flatMapLatest { rawQuery ->
            flow {
                val query = rawQuery.trim().lowercase()
                if (query.length <= 2) {
                    emit(ApiResult.Idle())
                    return@flow
                }
                // Cache Hit
                val cachedResult = searchCache[query]
                if (cachedResult != null) {
                    emit(ApiResult.Success(cachedResult))
                    return@flow
                }
                // Network Call
                emit(ApiResult.Loading())
                try {
                    val result = appRepo.searchCities(query)
                    // Save To Cache safely
                    if (result is ApiResult.Success && result.data != null) {
                        synchronized(searchCache) {
                            if (searchCache.size >= MAX_CACHE_SIZE) {
                                val firstKey = searchCache.keys.firstOrNull()
                                if (firstKey != null) searchCache.remove(firstKey)
                            }
                            searchCache[query] = result.data
                        }
                    }
                    emit(result)
                } catch (e: Exception) {
                    emit(ApiResult.Error(e.message ?: "Unknown Network Error"))
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ApiResult.Idle()
        )
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun getWeather(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _weatherData.value = ApiResult.Loading()
            _weatherData.value = appRepo.getWeather(latitude, longitude)
        }
    }
}
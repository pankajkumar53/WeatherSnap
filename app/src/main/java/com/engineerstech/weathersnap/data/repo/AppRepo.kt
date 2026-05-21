package com.engineerstech.weathersnap.data.repo

import com.engineerstech.weathersnap.data.api.ApiResult
import com.engineerstech.weathersnap.data.api.SearchApiService
import com.engineerstech.weathersnap.data.api.WeatherApiService
import com.engineerstech.weathersnap.data.api.makeApiCall
import com.engineerstech.weathersnap.data.local.ReportDao
import com.engineerstech.weathersnap.data.local.ReportEntity
import com.engineerstech.weathersnap.domain.models.SearchResults
import com.engineerstech.weathersnap.domain.models.WeatherResponse
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AppRepo @Inject constructor(
    private val weatherApiService: WeatherApiService,
    private val searchApiService: SearchApiService,
    private val reportDao: ReportDao
) {

    suspend fun getWeather(latitude: Double, longitude: Double): ApiResult<WeatherResponse> {
        return makeApiCall { weatherApiService.getWeather(latitude, longitude) }
    }

    suspend fun searchCities(name: String): ApiResult<SearchResults> {
        return makeApiCall { searchApiService.searchCities(name) }
    }

    suspend fun saveReport(report: ReportEntity) {
        reportDao.insertReport(report)
    }

    fun getAllReports(): Flow<List<ReportEntity>> {
        return reportDao.getAllReports()
    }

    fun getReportsCount(): Flow<Int> {
        return reportDao.getReportsCount()
    }
}

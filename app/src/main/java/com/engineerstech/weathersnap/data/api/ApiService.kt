package com.engineerstech.weathersnap.data.api

import com.engineerstech.weathersnap.domain.models.Current
import com.engineerstech.weathersnap.domain.models.SearchResults
import com.engineerstech.weathersnap.domain.models.WeatherResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface SearchApiService {

    @GET("v1/search")
    suspend fun searchCities(
        @Query("name") name: String,
    ): Response<SearchResults>
}


interface WeatherApiService {
    @GET("v1/forecast")
    suspend fun getWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m,relative_humidity_2m,pressure_msl,wind_speed_10m,weather_code",
    ): Response<WeatherResponse>

}
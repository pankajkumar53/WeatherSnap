package com.engineerstech.weathersnap.domain.models

data class SearchResults(
    val generationtime_ms: Double,
    val results: List<Result>
)
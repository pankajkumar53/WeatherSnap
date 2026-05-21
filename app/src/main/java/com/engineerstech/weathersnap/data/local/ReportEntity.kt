package com.engineerstech.weathersnap.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_reports")
data class ReportEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val cityName: String,
    val temperature: Double,
    val condition: String,
    val humidity: Int,
    val windSpeed: Double,
    val pressure: Double,
    val notes: String,
    val imagePath: String,
    val originalSize: Long,
    val compressedSize: Long,
    val timestamp: Long = System.currentTimeMillis()
)

package com.engineerstech.weathersnap.ui.report

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.engineerstech.weathersnap.data.api.ApiResult
import com.engineerstech.weathersnap.data.local.ReportEntity
import com.engineerstech.weathersnap.data.repo.AppRepo
import com.engineerstech.weathersnap.domain.models.WeatherResponse
import com.engineerstech.weathersnap.ui.home.getWeatherCondition
import com.engineerstech.weathersnap.util.DraftState
import com.engineerstech.weathersnap.util.SavedStateKeys.IMAGE_COMPRESSED_SIZE
import com.engineerstech.weathersnap.util.SavedStateKeys.REPORT_IMAGE_PATH
import com.engineerstech.weathersnap.util.SavedStateKeys.REPORT_NOTES
import com.engineerstech.weathersnap.util.SavedStateKeys.IMAGE_ORIGINAL_SIZE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val appRepo: AppRepo,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val allReports: StateFlow<List<ReportEntity>> = appRepo.getAllReports()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reportsCount: StateFlow<Int> = appRepo.getReportsCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val notes = DraftState(savedStateHandle, REPORT_NOTES, "")
    val capturedImagePath = DraftState<String?>(savedStateHandle, REPORT_IMAGE_PATH, null)
    val originalSize = DraftState(savedStateHandle, IMAGE_ORIGINAL_SIZE, 0L)
    val compressedSize = DraftState(savedStateHandle, IMAGE_COMPRESSED_SIZE, 0L)

    private val _saveState = MutableStateFlow<ApiResult<Unit>>(ApiResult.Idle())
    val saveState: StateFlow<ApiResult<Unit>> = _saveState.asStateFlow()

    fun onNotesChanged(value: String) = notes.update(value)

    fun onImageCaptured(imagePath: String, origSize: Long, compSize: Long) {
        capturedImagePath.flow.value?.let { oldPath ->
            viewModelScope.launch(Dispatchers.IO) {
                File(oldPath).takeIf { it.exists() }?.delete()
            }
        }
        capturedImagePath.update(imagePath)
        originalSize.update(origSize)
        compressedSize.update(compSize)
    }

    fun saveReport(
        cityName: String,
        weather: WeatherResponse,
        notes: String,
        imagePath: String,
        originalSizeBytes: Long,
        compressedSizeBytes: Long
    ) {
        viewModelScope.launch {
            _saveState.value = ApiResult.Loading()
            val result = withContext(Dispatchers.IO) {
                try {
                    appRepo.saveReport(
                        ReportEntity(
                            cityName = cityName,
                            temperature = weather.current.temperature_2m,
                            condition = getWeatherCondition(weather.current.weather_code),
                            humidity = weather.current.relative_humidity_2m,
                            windSpeed = weather.current.wind_speed_10m,
                            pressure = weather.current.pressure_msl,
                            notes = notes,
                            imagePath = imagePath,
                            originalSize = originalSizeBytes,
                            compressedSize = compressedSizeBytes
                        )
                    )
                    ApiResult.Success(Unit)
                } catch (e: Exception) {
                    ApiResult.Error(e.message ?: "Failed to save report")
                }
            }
            _saveState.value = result
            if (result is ApiResult.Success) clearDraft(deleteImageFile = false)
        }
    }

    fun clearDraft(deleteImageFile: Boolean) {
        if (deleteImageFile) {
            capturedImagePath.flow.value?.let { path ->
                viewModelScope.launch(Dispatchers.IO) {
                    File(path).takeIf { it.exists() }?.delete()
                }
            }
        }
        notes.clear("")
        capturedImagePath.clear(null)
        originalSize.clear(0L)
        compressedSize.clear(0L)
    }
}

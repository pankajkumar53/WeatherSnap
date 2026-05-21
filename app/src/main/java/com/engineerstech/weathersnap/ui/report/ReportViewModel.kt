package com.engineerstech.weathersnap.ui.report

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.engineerstech.weathersnap.data.api.ApiResult
import com.engineerstech.weathersnap.data.local.ReportEntity
import com.engineerstech.weathersnap.data.repo.AppRepo
import com.engineerstech.weathersnap.domain.models.WeatherResponse
import com.engineerstech.weathersnap.ui.home.getWeatherCondition
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
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val reportsCount: StateFlow<Int> = appRepo.getReportsCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    // ─── Draft State (SavedStateHandle → rotation/process-death safe) ───────────

    private val _notes = MutableStateFlow(
        savedStateHandle.get<String>(KEY_NOTES) ?: ""
    )
    val notes: StateFlow<String> = _notes.asStateFlow()

    private val _capturedImagePath = MutableStateFlow(
        savedStateHandle.get<String>(KEY_IMAGE_PATH)
    )
    val capturedImagePath: StateFlow<String?> = _capturedImagePath.asStateFlow()

    private val _originalSize = MutableStateFlow(
        savedStateHandle.get<Long>(KEY_ORIGINAL_SIZE) ?: 0L
    )
    val originalSize: StateFlow<Long> = _originalSize.asStateFlow()

    private val _compressedSize = MutableStateFlow(
        savedStateHandle.get<Long>(KEY_COMPRESSED_SIZE) ?: 0L
    )
    val compressedSize: StateFlow<Long> = _compressedSize.asStateFlow()

    // ─── Save Report State ───────────────────────────────────────────────────────

    private val _saveState = MutableStateFlow<ApiResult<Unit>>(ApiResult.Idle())
    val saveState: StateFlow<ApiResult<Unit>> = _saveState.asStateFlow()

    // ─── Draft Updaters ──────────────────────────────────────────────────────────

    fun onNotesChanged(value: String) {
        _notes.value = value
        savedStateHandle[KEY_NOTES] = value
    }

    fun onImageCaptured(imagePath: String, origSize: Long, compSize: Long) {
        _capturedImagePath.value?.let { oldPath ->
            viewModelScope.launch(Dispatchers.IO) {
                val oldFile = File(oldPath)
                if (oldFile.exists()) oldFile.delete()
            }
        }

        _capturedImagePath.value = imagePath
        _originalSize.value = origSize
        _compressedSize.value = compSize

        savedStateHandle[KEY_IMAGE_PATH] = imagePath
        savedStateHandle[KEY_ORIGINAL_SIZE] = origSize
        savedStateHandle[KEY_COMPRESSED_SIZE] = compSize
    }

    // ─── Save Report ─────────────────────────────────────────────────────────────

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
                    val report = ReportEntity(
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
                    appRepo.saveReport(report = report)
                    ApiResult.Success(Unit)
                } catch (e: Exception) {
                    ApiResult.Error(e.message ?: "Failed to save report")
                }
            }

            _saveState.value = result

            // Draft clear karo aur original file delete karo — sirf success par
            if (result is ApiResult.Success) {
                clearDraft(deleteImageFile = false) // compressed file Room mein save hai
            }
        }
    }

    // ─── Cleanup ─────────────────────────────────────────────────────────────────

    fun onDiscardDraft() {
        clearDraft(deleteImageFile = true)
    }

    private fun clearDraft(deleteImageFile: Boolean) {
        if (deleteImageFile) {
            _capturedImagePath.value?.let { path ->
                viewModelScope.launch(Dispatchers.IO) {
                    val file = File(path)
                    if (file.exists()) file.delete()
                }
            }
        }

        _notes.value = ""
        _capturedImagePath.value = null
        _originalSize.value = 0L
        _compressedSize.value = 0L

        savedStateHandle.remove<String>(KEY_NOTES)
        savedStateHandle.remove<String>(KEY_IMAGE_PATH)
        savedStateHandle.remove<Long>(KEY_ORIGINAL_SIZE)
        savedStateHandle.remove<Long>(KEY_COMPRESSED_SIZE)
    }

    companion object {
        private const val KEY_NOTES = "draft_notes"
        private const val KEY_IMAGE_PATH = "draft_image_path"
        private const val KEY_ORIGINAL_SIZE = "draft_original_size"
        private const val KEY_COMPRESSED_SIZE = "draft_compressed_size"
    }
}

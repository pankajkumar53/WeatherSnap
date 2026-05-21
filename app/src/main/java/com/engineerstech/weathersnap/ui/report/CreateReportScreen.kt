package com.engineerstech.weathersnap.ui.report

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.toRoute
import com.engineerstech.weathersnap.data.api.ApiResult
import com.engineerstech.weathersnap.ui.component.ApiResultContent
import com.engineerstech.weathersnap.ui.component.CameraScreen
import com.engineerstech.weathersnap.ui.component.CustomChip
import com.engineerstech.weathersnap.ui.component.CustomColumn
import com.engineerstech.weathersnap.ui.component.HeaderCard
import com.engineerstech.weathersnap.ui.home.HomeViewModel
import com.engineerstech.weathersnap.ui.home.WeatherCard
import com.engineerstech.weathersnap.ui.navigation.LocalNavigationProvider
import com.engineerstech.weathersnap.ui.navigation.Routes
import com.engineerstech.weathersnap.ui.theme.AppGray
import com.engineerstech.weathersnap.ui.theme.DarkYellow
import com.engineerstech.weathersnap.ui.theme.GreenColor
import com.engineerstech.weathersnap.ui.theme.LightYellow

@Composable
fun CreateReportScreen() {

    val navBackStackEntry = LocalNavigationProvider.current.currentBackStackEntry
    val reportArgs = navBackStackEntry?.toRoute<Routes.CreateReport>()

    val cityName = reportArgs?.cityName
    val lat = reportArgs?.lat
    val long = reportArgs?.long

    val homeViewModel: HomeViewModel = hiltViewModel()
    val reportViewModel: ReportViewModel = hiltViewModel()

    val weatherData by homeViewModel.weatherData.collectAsState()
    val saveState by reportViewModel.saveState.collectAsState()
    val capturedImagePath by reportViewModel.capturedImagePath.collectAsState()
    val originalSize by reportViewModel.originalSize.collectAsState()
    val compressedSize by reportViewModel.compressedSize.collectAsState()
    val notes by reportViewModel.notes.collectAsState()

    val currentWeather = (weatherData as? ApiResult.Success)?.data
    val isSaving = saveState is ApiResult.Loading

    val navController = LocalNavigationProvider.current
    val context = LocalContext.current
    var showCamera by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (lat != null && long != null) {
            homeViewModel.getWeather(lat, long)
        }
    }

    LaunchedEffect(saveState) {
        if (saveState is ApiResult.Success) {
            navController.navigate(Routes.SavedReports)
        }
    }

    if (showCamera) {
        CameraScreen(
            onImageCaptured = { result ->
                reportViewModel.onImageCaptured(
                    imagePath = result.compressedFile.absolutePath,
                    origSize = result.originalSizeBytes,
                    compSize = result.compressedSizeBytes
                )
                showCamera = false
            },
            onClose = { showCamera = false }
        )
        return
    }

    CustomColumn {

        HeaderCard(
            title = "Create Report",
            subTitle = "Capture, compress, annotate",
            buttonTitle = "Back"
        ) {
            reportViewModel.onDiscardDraft()
            navController.popBackStack()
        }

        Spacer(modifier = Modifier.height(16.dp))

        ApiResultContent(weatherData) { weather ->
            WeatherCard(
                weather = weather,
                cityName = cityName ?: "Unknown Location",
                buttonEnabled = false,
                onCreateReport = {}
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Photo Preview Card
        PhotoPreviewCard(
            capturedImagePath, originalSize, compressedSize, { showCamera = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Field Notes
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = AppGray),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "Field Notes",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { reportViewModel.onNotesChanged(it) },
                    minLines = 4,
                    placeholder = { Text("Notes", color = Color.Gray) },
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFCDDC39),
                        unfocusedBorderColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val path = capturedImagePath
                if (currentWeather != null && path != null) {
                    reportViewModel.saveReport(
                        cityName = cityName ?: "Unknown",
                        weather = currentWeather,
                        notes = notes,
                        imagePath = path,
                        originalSizeBytes = originalSize,
                        compressedSizeBytes = compressedSize
                    )
                } else {
                    Toast.makeText(context, "add all field", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = DarkYellow),
            enabled = capturedImagePath != null && !isSaving
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = DarkYellow,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Save Report")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun PhotoPreviewCard(
    capturedImagePath: String?,
    originalSize: Long,
    compressedSize: Long,
    showCameraChanged: (Boolean) -> Unit
) {

    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showCameraChanged(true)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppGray),
        shape = RoundedCornerShape(16.dp)
    ) {
        if (capturedImagePath != null) {
            AnimatedContent(
                targetState = capturedImagePath,
                transitionSpec = { fadeIn(tween(500)) togetherWith fadeOut(tween(300)) },
                label = "ImagePreview"
            ) { path ->
                path.let {
                    val bitmap = remember(it) { BitmapFactory.decodeFile(it) }
                    bitmap?.let { bmp ->
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = "Captured photo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .padding(12.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        } else {
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)) {
                Text(
                    "Photo preview",
                    color = Color.LightGray,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        Button(
            onClick = {
                val granted = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED

                if (granted) {
                    showCameraChanged(true)
                } else permissionLauncher.launch(Manifest.permission.CAMERA)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DarkYellow),
        ) {
            Text(
                text = if (capturedImagePath != null) "Retake Photo" else "Capture Photo",
            )
        }

        if (originalSize > 0L && compressedSize > 0L) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CustomChip(
                    label = "Original",
                    value = formatSize(originalSize),
                    modifier = Modifier.weight(1f),
                    color = GreenColor
                )
                CustomChip(
                    label = "Compressed",
                    value = formatSize(compressedSize),
                    modifier = Modifier.weight(1f),
                    color = LightYellow
                )
            }
        }
    }


}

private fun formatSize(bytes: Long): String {
    return when {
        bytes >= 1024 * 1024 -> "%.2f MB".format(bytes / (1024f * 1024f))
        bytes >= 1024 -> "%.1f KB".format(bytes / 1024f)
        else -> "$bytes B"
    }
}
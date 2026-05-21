package com.engineerstech.weathersnap.ui.component

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.engineerstech.weathersnap.ui.theme.DarkYellow
import java.io.File
import java.io.FileOutputStream

data class CaptureResult(
    val originalFile: File,
    val compressedFile: File,
    val originalSizeBytes: Long,
    val compressedSizeBytes: Long
)

@Composable
fun CameraScreen(
    onImageCaptured: (CaptureResult) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var isCapturing by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {

        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                ProcessCameraProvider.getInstance(ctx).addListener({
                    val cameraProvider = ProcessCameraProvider.getInstance(ctx).get()
                    val preview = Preview.Builder().build()
                        .also { it.setSurfaceProvider(previewView.surfaceProvider) }
                    val capture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build()
                    imageCapture = capture
                    runCatching {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            capture
                        )
                    }
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Top Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Custom Camera",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
            OutlinedButton(
                onClick = onClose,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Text("Close")
            }
        }

        // Capture Button
        Button(
            onClick = {
                if (!isCapturing) {
                    isCapturing = true
                    captureImage(
                        context = context,
                        imageCapture = imageCapture,
                        onCaptured = { result ->
                            isCapturing = false
                            onImageCaptured(result)
                        },
                        onError = { isCapturing = false }
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.BottomCenter),
            colors = ButtonDefaults.buttonColors(containerColor = DarkYellow),
            enabled = !isCapturing
        ) {
            Text(
                text = if (isCapturing) "Capturing..." else "Capture",
            )
        }
    }
}

private fun captureImage(
    context: Context,
    imageCapture: ImageCapture?,
    onCaptured: (CaptureResult) -> Unit,
    onError: () -> Unit
) {
    val originalFile = File(context.cacheDir, "orig_${System.currentTimeMillis()}.jpg")

    imageCapture?.takePicture(
        ImageCapture.OutputFileOptions.Builder(originalFile).build(),
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val originalSize = originalFile.length()
                val compressedFile = compressImage(context, originalFile)
                onCaptured(
                    CaptureResult(
                        originalFile = originalFile,
                        compressedFile = compressedFile,
                        originalSizeBytes = originalSize,
                        compressedSizeBytes = compressedFile.length()
                    )
                )
            }
            override fun onError(e: ImageCaptureException) {
                e.printStackTrace()
                onError()
            }
        }
    )
}

private fun compressImage(context: Context, originalFile: File): File {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(originalFile.absolutePath, bounds)

    val maxDimension = 1080
    val largerSide = maxOf(bounds.outWidth, bounds.outHeight)
    var sampleSize = 1
    while (largerSide / sampleSize > maxDimension) sampleSize *= 2

    val bitmap = BitmapFactory.decodeFile(
        originalFile.absolutePath,
        BitmapFactory.Options().apply { inSampleSize = sampleSize }
    )

    val compressedFile = File(context.cacheDir, "comp_${System.currentTimeMillis()}.jpg")
    FileOutputStream(compressedFile).use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, out)
    }
    bitmap.recycle()
    return compressedFile
}

package com.engineerstech.weathersnap.ui.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.engineerstech.weathersnap.data.local.ReportEntity
import com.engineerstech.weathersnap.ui.component.CustomChip
import com.engineerstech.weathersnap.ui.component.CustomColumn
import com.engineerstech.weathersnap.ui.component.HeaderCard
import com.engineerstech.weathersnap.ui.navigation.LocalNavigationProvider
import com.engineerstech.weathersnap.ui.theme.AppGray
import com.engineerstech.weathersnap.ui.theme.DarkYellow
import com.engineerstech.weathersnap.ui.theme.GreenColor
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SavedReportScreen() {
    val viewModel: ReportViewModel = hiltViewModel()
    val reports by viewModel.allReports.collectAsState()
    val count by viewModel.reportsCount.collectAsState()
    val navController = LocalNavigationProvider.current

    // Set scrollable = false because LazyColumn handles scrolling internally
    CustomColumn(scrollable = false) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                HeaderCard(
                    title = "Saved Reports",
                    subTitle = "$count ${if (count == 1) "report" else "reports"} stored locally",
                    buttonTitle = "Back",
                    onClick = { navController.popBackStack() }
                )
            }

            if (reports.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillParentMaxSize()
                            .padding(bottom = 100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "No reports saved yet", color = Color.Gray)
                    }
                }
            } else {
                items(reports) { report ->
                    ReportItemCard(report = report)
                }
            }
        }
    }
}

@Composable
fun ReportItemCard(report: ReportEntity) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(report.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppGray.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            AsyncImage(
                model = File(report.imagePath),
                contentDescription = "Weather Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = report.cityName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = report.condition,
                        color = Color.LightGray,
                        fontSize = 14.sp
                    )
                    Text(
                        text = formattedDate,
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF3E412A))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "${report.temperature.toInt()}°C",
                        color = DarkYellow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CustomChip(
                    label = "Original",
                    value = formatFileSize(report.originalSize),
                    color = DarkYellow,
                    modifier = Modifier.weight(1f)
                )
                CustomChip(
                    label = "Compressed",
                    value = formatFileSize(report.compressedSize),
                    color = GreenColor,
                    modifier = Modifier.weight(1f)
                )
            }

            if (report.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = report.notes,
                        color = Color.LightGray,
                    )
                }
            }
        }
    }
}

private fun formatFileSize(size: Long): String {
    if (size <= 0) return "0 KB"
    val units = arrayOf("B", "KB", "MB", "GB")
    val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
    val value = size / Math.pow(1024.0, digitGroups.toDouble())
    return String.format(Locale.getDefault(), "%.0f %s", value, units[digitGroups])
}

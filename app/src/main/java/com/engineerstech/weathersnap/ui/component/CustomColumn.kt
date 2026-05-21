package com.engineerstech.weathersnap.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.engineerstech.weathersnap.ui.theme.BackGroundColor
import com.engineerstech.weathersnap.ui.theme.BackgroundBottomColor
import com.engineerstech.weathersnap.ui.theme.BackgroundTopColor

@Composable
fun CustomColumn(scrollable: Boolean = true, content: @Composable ColumnScope.() -> Unit) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        BackgroundTopColor,
                        BackgroundBottomColor,
                        BackGroundColor
                    )
                )
            )
            .padding(12.dp)
            .then(
                if (scrollable) {
                    Modifier.verticalScroll(scrollState)
                } else {
                    Modifier
                }
            )
    ) {
        content()
    }
}
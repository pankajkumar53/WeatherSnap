package com.engineerstech.weathersnap.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.engineerstech.weathersnap.ui.theme.BlackColor
import com.engineerstech.weathersnap.ui.theme.DarkYellow
import com.engineerstech.weathersnap.ui.theme.SkyBlue

@Composable
fun HeaderCard(title: String, subTitle: String, buttonTitle: String, onClick: () -> Unit) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(DarkYellow, SkyBlue)
                ),
                shape = CardDefaults.shape
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    title, style = MaterialTheme.typography.headlineLarge.copy(
                        color = BlackColor,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Text(
                    subTitle,
                    fontSize = 11.sp,
                    color = BlackColor
                )
            }
            Button(
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.padding(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BlackColor.copy(.9f),
                    contentColor = Color.White
                ),
                onClick = { }
            ) {
                Text(buttonTitle, color = DarkYellow)
            }
        }
    }

}
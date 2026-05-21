package com.engineerstech.weathersnap.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.engineerstech.weathersnap.ui.theme.DarkYellow

@Composable
fun SearchField(searchQuery: String, onValueChange: (String) -> Unit, onClick: () -> Unit) {

    Row(
        modifier = Modifier.fillMaxWidth().padding(12.dp)
    ) {
        Column {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    onValueChange(it)
                },
                modifier = Modifier.fillMaxWidth(.7f),
                label = {
                    Text("City")
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DarkYellow,
                    focusedTextColor = DarkYellow,
                    focusedLabelColor = DarkYellow
                )
            )
            Text(
                "Enter more than two letters to start suggestions",
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Button(
            onClick = {
                onClick()
            },
            modifier = Modifier.align(Alignment.CenterVertically).padding(bottom = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DarkYellow)
        ) {
            Text("Search", fontSize = 12.sp)
        }
    }

}
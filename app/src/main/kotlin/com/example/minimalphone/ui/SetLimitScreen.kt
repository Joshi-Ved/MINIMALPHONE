package com.example.minimalphone.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun SetLimitScreen(
    currentLimitMinutes: Int,
    onSave: (Int) -> Unit,
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)

    var input by rememberSaveable(currentLimitMinutes) { mutableStateOf(currentLimitMinutes.toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = "Daily Limit", style = MaterialTheme.typography.headlineMedium)
        Text(text = "Set your daily screen-time budget in minutes.", style = MaterialTheme.typography.bodyMedium)

        OutlinedTextField(
            value = input,
            onValueChange = { input = it.filter(Char::isDigit) },
            label = { Text("Minutes") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        Button(
            onClick = {
                val value = input.toIntOrNull()?.coerceAtLeast(1) ?: currentLimitMinutes
                onSave(value)
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Save")
        }

        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }
    }
}

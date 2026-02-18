package com.example.minimalphone.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.minimalphone.ui.theme.FocusLiteTheme
import com.example.minimalphone.viewmodel.DashboardViewModel
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// Settings Screen to manage daily usage limit
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SettingsScreen(
    viewModel: DashboardViewModel = viewModel(),
    onBackClick: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    SettingsContent(
        currentDailyLimit = state.dailyLimitMinutes,
        onDailyLimitChanged = { newLimit ->
            viewModel.updateDailyLimit(newLimit)
        },
        onBackClick = onBackClick,
    )
}

@Composable
private fun SettingsContent(
    currentDailyLimit: Int,
    onDailyLimitChanged: (Int) -> Unit,
    onBackClick: () -> Unit,
) {
    var inputValue by remember { mutableStateOf(currentDailyLimit.toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top,
    ) {
        // ── Header with back button ──────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
            Text(
                text = "Settings",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 8.dp),
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // ── Daily Limit Setting ──────────────────────────────────────────
        Text(
            text = "Daily Screen-Time Limit",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Set your maximum allowed screen time per day (in minutes)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ── Input field for minutes ──────────────────────────────────────
        OutlinedTextField(
            value = inputValue,
            onValueChange = { inputValue = it },
            label = { Text("Minutes") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ── Save button ──────────────────────────────────────────────────
        Button(
            onClick = {
                val newLimit = inputValue.toIntOrNull() ?: currentDailyLimit
                if (newLimit > 0) {
                    onDailyLimitChanged(newLimit)
                    inputValue = newLimit.toString()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Text("Save")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Help text ────────────────────────────────────────────────────
        Text(
            text = "Current limit: $currentDailyLimit minutes",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
        )

        Spacer(modifier = Modifier.weight(1f))

        // ── Info section ─────────────────────────────────────────────────
        Text(
            text = "How it works",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your daily limit is saved in DataStore (on-device storage). It syncs across app restarts and survives phone rotations. FocusLite checks your actual screen time from UsageStatsManager and compares it to this limit.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
        )
    }
}

@Preview(showBackground = true, name = "Settings – Light")
@Composable
private fun SettingsPreviewLight() {
    FocusLiteTheme(darkTheme = false) {
        SettingsContent(
            currentDailyLimit = 120,
            onDailyLimitChanged = {},
            onBackClick = {},
        )
    }
}

@Preview(showBackground = true, name = "Settings – Dark")
@Composable
private fun SettingsPreviewDark() {
    FocusLiteTheme(darkTheme = true) {
        SettingsContent(
            currentDailyLimit = 120,
            onDailyLimitChanged = {},
            onBackClick = {},
        )
    }
}

package com.example.minimalphone.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.minimalphone.domain.model.AppBlockMode

@Composable
fun SettingsScreen(
    lockModeEnabled: Boolean,
    premiumEnabled: Boolean,
    blockMode: AppBlockMode,
    onLockModeChanged: (Boolean) -> Unit,
    onPremiumChanged: (Boolean) -> Unit,
    onSetLimit: () -> Unit,
    onOpenAppSelection: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)

        Text("Lock Mode", style = MaterialTheme.typography.bodyLarge)
        Switch(checked = lockModeEnabled, onCheckedChange = onLockModeChanged)

        Text("Premium", style = MaterialTheme.typography.bodyLarge)
        Switch(checked = premiumEnabled, onCheckedChange = onPremiumChanged)

        Text("Block Strategy: ${blockMode.name}", style = MaterialTheme.typography.bodyMedium)

        Button(onClick = onSetLimit, modifier = Modifier.fillMaxWidth()) {
            Text("Set Daily Limit")
        }
        Button(onClick = onOpenAppSelection, modifier = Modifier.fillMaxWidth()) {
            Text("App Blocking List")
        }
        Button(onClick = onOpenAccessibilitySettings, modifier = Modifier.fillMaxWidth()) {
            Text("Enable Accessibility")
        }
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }
    }
}

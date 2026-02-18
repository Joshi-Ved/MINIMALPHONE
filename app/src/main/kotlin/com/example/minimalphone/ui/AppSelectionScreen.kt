package com.example.minimalphone.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.minimalphone.domain.model.AppBlockMode
import com.example.minimalphone.domain.model.InstalledApp

@Composable
fun AppSelectionScreen(
    apps: List<InstalledApp>,
    selectedPackages: Set<String>,
    mode: AppBlockMode,
    onTogglePackage: (String) -> Unit,
    onModeChanged: (AppBlockMode) -> Unit,
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("App Selection", style = MaterialTheme.typography.headlineMedium)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Block Selected",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onModeChanged(AppBlockMode.BLOCK_SELECTED) },
                color = if (mode == AppBlockMode.BLOCK_SELECTED) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.secondary,
            )
            Text(
                text = "Allow Whitelist",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onModeChanged(AppBlockMode.ALLOW_ONLY_WHITELIST) },
                color = if (mode == AppBlockMode.ALLOW_ONLY_WHITELIST) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.secondary,
            )
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(items = apps, key = { it.packageName }) { app ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTogglePackage(app.packageName) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = selectedPackages.contains(app.packageName),
                        onCheckedChange = { onTogglePackage(app.packageName) },
                    )
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text(text = app.label, style = MaterialTheme.typography.bodyLarge)
                        Text(text = app.packageName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }
    }
}

package com.example.minimalphone.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.minimalphone.ui.state.DashboardUiState
import com.example.minimalphone.ui.state.formatMinutes

// ─────────────────────────────────────────────────────────────────────────────
// 📘 BEGINNER CONCEPT: Why is Compose "reactive"?
// ─────────────────────────────────────────────────────────────────────────────
// In traditional Android (XML layouts), you had to MANUALLY find a TextView
// and call textView.text = "new value" every time something changed.
//
// Jetpack Compose works differently:
//   1. You describe WHAT the UI should look like for a given state.
//   2. When the state changes, Compose automatically re-runs ("recomposes")
//      only the parts of the UI that depend on that state.
//
// This is called "declarative UI" — you declare the result, and the
// framework figures out how to get there.
// ─────────────────────────────────────────────────────────────────────────────

// ─────────────────────────────────────────────────────────────────────────────
// 📘 BEGINNER CONCEPT: What happens on recomposition?
// ─────────────────────────────────────────────────────────────────────────────
// "Recomposition" means Compose re-executes a @Composable function because
// some state it reads has changed.
//
// Example flow:
//   1. ViewModel fetches new usage data.
//   2. ViewModel updates MutableStateFlow with the new minutes.
//   3. collectAsStateWithLifecycle() detects the new value.
//   4. Compose sees that `state` changed and re-runs DashboardScreen.
//   5. The Text composables now show the latest real usage.
//
// Important: Compose is SMART — it only recomposes the composables that
// actually read the changed data.  Everything else stays untouched.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun DashboardScreen(
    state: DashboardUiState,
    onOpenUsageSettingsClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onSetLimitClick: () -> Unit,
    onAppSelectionClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top,
    ) {
        Text("FocusLite", style = MaterialTheme.typography.headlineLarge)
        Text("Cold. Minimal. Functional.", color = MaterialTheme.colorScheme.secondary)
        Spacer(modifier = Modifier.height(24.dp))

        when (state) {
            DashboardUiState.Loading -> {
                Text("Loading usage…")
            }

            is DashboardUiState.PermissionRequired -> {
                Text("Usage Access Required", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text(state.helpText, color = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onOpenUsageSettingsClick, modifier = Modifier.fillMaxWidth()) {
                    Text("Open Usage Settings")
                }
            }

            is DashboardUiState.Ready -> {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Today", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        formatMinutes(state.usedMinutes),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Remaining", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        formatMinutes(state.remainingMinutes),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (state.isOverLimit) {
                    Text("Limit Reached", color = MaterialTheme.colorScheme.error)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onRefreshClick, modifier = Modifier.fillMaxWidth()) { Text("Refresh") }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onSetLimitClick, modifier = Modifier.fillMaxWidth()) { Text("Set Daily Limit") }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onAppSelectionClick, modifier = Modifier.fillMaxWidth()) { Text("App Blocking List") }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onSettingsClick, modifier = Modifier.fillMaxWidth()) { Text("Settings") }
    }
}

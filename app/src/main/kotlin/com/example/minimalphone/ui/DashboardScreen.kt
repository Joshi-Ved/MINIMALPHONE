package com.example.minimalphone.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.minimalphone.data.UsageState
import com.example.minimalphone.ui.theme.FocusLiteTheme
import com.example.minimalphone.viewmodel.DashboardViewModel

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
//   1. User taps "Increase Usage".
//   2. ViewModel updates usedMinutes from 80 → 90 inside MutableStateFlow.
//   3. collectAsStateWithLifecycle() detects the new value.
//   4. Compose sees that `state` changed and re-runs DashboardScreen.
//   5. The Text composables now show "1h 30m" instead of "1h 20m".
//
// Important: Compose is SMART — it only recomposes the composables that
// actually read the changed data.  Everything else stays untouched.
// ─────────────────────────────────────────────────────────────────────────────

/**
 * The main dashboard screen.
 *
 * @param viewModel  Provided automatically by [viewModel()] — you rarely
 *                   need to create one yourself.
 * @param onIncreaseClick  Called when the user taps "Increase Usage".
 * @param onResetClick     Called when the user taps "Reset".
 */
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel(),
) {
    // ── Observe the ViewModel's state ────────────────────────────────────
    // collectAsStateWithLifecycle() converts the StateFlow into a Compose
    // State object.  It automatically stops collecting when the screen is
    // not visible (saving battery).
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Delegate to a "stateless" version so we can preview it easily.
    DashboardContent(
        state = state,
        onIncreaseClick = viewModel::increaseUsage,
        onResetClick = viewModel::resetUsage,
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Stateless content — receives data & callbacks, draws UI.
// Having a separate stateless composable makes previews and testing easier.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DashboardContent(
    state: UsageState,
    onIncreaseClick: () -> Unit,
    onResetClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top,
    ) {
        // ── App title ────────────────────────────────────────────────────
        Text(
            text = "FocusLite",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your screen-time dashboard",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
        )

        Spacer(modifier = Modifier.height(48.dp))

        // ── Today's Usage ────────────────────────────────────────────────
        StatRow(label = "Today's Usage", value = state.usedFormatted)

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 16.dp),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f),
        )

        // ── Remaining ────────────────────────────────────────────────────
        StatRow(
            label = "Remaining",
            value = state.remainingFormatted,
            // Show a warning color when the limit has been reached.
            valueColor = if (state.isOverLimit) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onBackground
            },
        )

        if (state.isOverLimit) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Daily limit reached!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }

        Spacer(modifier = Modifier.weight(1f))  // push buttons to bottom

        // ── Buttons ──────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // "Increase Usage" — filled button
            Button(
                onClick = onIncreaseClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Text("Increase Usage")
            }

            // "Reset" — outlined button
            OutlinedButton(
                onClick = onResetClick,
                modifier = Modifier.weight(1f),
            ) {
                Text("Reset")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Small reusable composable — a label on the left, a value on the right.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StatRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onBackground,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = valueColor,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 📘 BEGINNER CONCEPT: @Preview
// ─────────────────────────────────────────────────────────────────────────────
// @Preview lets you see the composable inside Android Studio WITHOUT running
// the app on a device.  Very useful for quick design iteration.
// ─────────────────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "Dashboard – Light")
@Composable
private fun DashboardPreviewLight() {
    FocusLiteTheme(darkTheme = false) {
        DashboardContent(
            state = UsageState(usedMinutes = 80, dailyLimitMinutes = 120),
            onIncreaseClick = {},
            onResetClick = {},
        )
    }
}

@Preview(showBackground = true, name = "Dashboard – Dark")
@Composable
private fun DashboardPreviewDark() {
    FocusLiteTheme(darkTheme = true) {
        DashboardContent(
            state = UsageState(usedMinutes = 130, dailyLimitMinutes = 120),
            onIncreaseClick = {},
            onResetClick = {},
        )
    }
}

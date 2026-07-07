package com.wolfscowl.vibronumber.presentation.screen.settings_screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wolfscowl.vibronumber.presentation.app.AppViewModelProvider
import com.wolfscowl.vibronumber.presentation.commons.UIDesign

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()


    // ── SCAFFOLD ─────────────────────────────────────────────────────────────────────────────────
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                colors = UIDesign.topAppBarColors
            )
        }
    ) { innerPadding ->
        // ── LAYOUT ───────────────────────────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "PTS Parameters",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Start)
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            // ── START HOLD FACTOR ──────────────────────────────────────────────────────────────
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "PTS Start Hold Factor",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = String.format("%.1f", uiState.ptsStartHoldFactor),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Slider(
                value = uiState.ptsStartHoldFactor,
                onValueChange = { viewModel.updateStartHoldFactor(it) },
                valueRange = 0.0f..2.0f,
                steps = 19,
                modifier = Modifier.height(32.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── END HOLD FACTOR ────────────────────────────────────────────────────────────────
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "PTS End Hold Factor",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = String.format("%.1f", uiState.ptsEndHoldFactor),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Slider(
                value = uiState.ptsEndHoldFactor,
                onValueChange = { viewModel.updateEndHoldFactor(it) },
                valueRange = 0.0f..2.0f,
                steps = 19,
                modifier = Modifier.height(32.dp)
            )
        }
    }
}

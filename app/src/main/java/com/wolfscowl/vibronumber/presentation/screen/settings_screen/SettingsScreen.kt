package com.wolfscowl.vibronumber.presentation.screen.settings_screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
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
import com.wolfscowl.vibronumber.presentation.screen.settings_screen.components.ParameterCard
import com.wolfscowl.vibronumber.presentation.screen.settings_screen.components.ParameterSlider

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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── GENERAL SECTION ──────────────────────────────────────────────────────────────────
            ParameterCard(title = "General Parameters") {
                ParameterSlider(
                    label = "Post Digit Delay (ms)",
                    value = uiState.postDigitDelayMs.toFloat(),
                    onValueChange = { viewModel.updatePostDigitDelay(it.toInt()) },
                    range = 0f..2000f,
                    steps = 19, // 100ms steps
                    valueFormatter = { String.format("%.0f", it) }
                )
            }

            // ── DSCR SECTION ─────────────────────────────────────────────────────────────────────
            ParameterCard(title = "DSCR Parameters") {
                // DSCR START
                ParameterSlider(
                    label = "DSCR Start Hold Factor",
                    value = uiState.dscrStartHoldFactor,
                    onValueChange = { viewModel.updateDscrStartHoldFactor(it) },
                    range = 1.0f..5.0f,
                    steps = 20
                )

                Spacer(modifier = Modifier.height(16.dp))

                // DSCR END
                ParameterSlider(
                    label = "DSCR End Hold Factor",
                    value = uiState.dscrEndHoldFactor,
                    onValueChange = { viewModel.updateDscrEndHoldFactor(it) },
                    range = 1.0f..5.0f,
                    steps = 20
                )
            }

            // ── ATM SECTION ──────────────────────────────────────────────────────────────────────
            ParameterCard(title = "ATM Parameters") {
                // ATM START
                ParameterSlider(
                    label = "ATM Start Hold Factor",
                    value = uiState.atmStartHoldFactor,
                    onValueChange = { viewModel.updateAtmStartHoldFactor(it) },
                    range = 1.0f..5.0f,
                    steps = 20
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ATM END
                ParameterSlider(
                    label = "ATM End Hold Factor",
                    value = uiState.atmEndHoldFactor,
                    onValueChange = { viewModel.updateAtmEndHoldFactor(it) },
                    range = 1.0f..5.0f,
                    steps = 20
                )
            }

            // ── PTS SECTION ──────────────────────────────────────────────────────────────────────
            ParameterCard(title = "PTS Parameters") {
                // PTS START
                ParameterSlider(
                    label = "PTS Start Hold Factor",
                    value = uiState.ptsStartHoldFactor,
                    onValueChange = { viewModel.updateStartHoldFactor(it) },
                    range = 0.0f..4.0f,
                    steps = 19
                )

                Spacer(modifier = Modifier.height(16.dp))

                // PTS END
                ParameterSlider(
                    label = "PTS End Hold Factor",
                    value = uiState.ptsEndHoldFactor,
                    onValueChange = { viewModel.updateEndHoldFactor(it) },
                    range = 0.0f..4.0f,
                    steps = 19
                )
            }
        }
    }
}




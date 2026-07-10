package com.wolfscowl.vibronumber.presentation.screen.home_screen

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wolfscowl.vibronumber.presentation.app.AppViewModelProvider
import com.wolfscowl.vibronumber.presentation.commons.UIDesign
import com.wolfscowl.vibronumber.presentation.model.VibroMode
import com.wolfscowl.vibronumber.presentation.screen.home_screen.components.VibrationGrid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeScreenViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // ── SCAFFOLD ─────────────────────────────────────────────────────────────────────────────────
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("VibroNumber") },
                actions = {
                    if (uiState.isConnected) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .widthIn(max = 120.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.BluetoothConnected,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = uiState.connectedDevice?.name ?: "Unknown Device",
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                modifier = Modifier.basicMarquee()
                            )
                        }
                    }
                },
                colors = UIDesign.topAppBarColors
            )
        }
    ) { innerPadding ->

        // ── LAYOUT ───────────────────────────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── 1. 3x3 SIMULATION GRID (Filler - Takes remaining space) ─────────────────────────
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                VibrationGrid(
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f),
                    intensities = uiState.actorIntensities,
                    onActorClick =
                        if (uiState.config.mode == VibroMode.TEST && uiState.isConnected)
                            viewModel::onActorClick
                        else
                            null
                )
            }

            // ── 2. CONTROLS (Priority - Drawn first at bottom) ──────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 0.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ── DIGIT SELECTION ─────────────────────────────────────────────────────────────
                Text(
                    text = "Select Digit",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Row 1: Digits 0-4
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (0..4).forEach { digit ->
                        val isSelected = uiState.config.digit == digit
                        Button(
                            onClick = { viewModel.updateDigit(digit) },
                            enabled = uiState.config.mode != VibroMode.TEST,
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text(text = digit.toString(), fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Row 2: Digits 5-9
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (5..9).forEach { digit ->
                        val isSelected = uiState.config.digit == digit
                        Button(
                            onClick = { viewModel.updateDigit(digit) },
                            enabled = uiState.config.mode != VibroMode.TEST,
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text(text = digit.toString(), fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ── MODE SELECTION ──────────────────────────────────────────────────────────────
                Text(
                    text = "Vibration Mode",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.align(Alignment.Start)
                )
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    VibroMode.entries.forEach { mode ->
                        val isSelected = uiState.config.mode == mode
                        OutlinedButton(
                            onClick = { viewModel.updateMode(mode) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Text(text = mode.abbreviation)
                        }
                        if (mode != VibroMode.entries.last()) {
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ── DURATION SLIDER ─────────────────────────────────────────────────────────────
                val durationLabel = if (uiState.config.mode == VibroMode.PTS) "Transition Time" else "Motor Duration"
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(durationLabel, style = MaterialTheme.typography.titleSmall)
                    Text(
                        text = "${uiState.config.durationMs} ms",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Slider(
                    value = uiState.config.durationMs.toFloat(),
                    onValueChange = { viewModel.updateDuration(it.toInt()) },
                    valueRange = 50f..2000f,
                    modifier = Modifier.height(32.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // ── INTENSITY SLIDER ────────────────────────────────────────────────────────────
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Intensity", style = MaterialTheme.typography.titleSmall)
                    Text(
                        text = "${uiState.config.intensityPct} %",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Slider(
                    value = uiState.config.intensityPct.toFloat(),
                    onValueChange = { viewModel.updateIntensity(it.toInt()) },
                    valueRange = 5f..100f,
                    modifier = Modifier.height(32.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // ── ACTION BUTTONS ─────────────────────────────────────────────────────────────
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Simulation Button
                    OutlinedButton(
                        onClick = { viewModel.simulate() },
                        enabled = uiState.config.mode != VibroMode.TEST,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SIMULATE")
                    }

                    // Send Button
                    Button(
                        onClick = { viewModel.sendConfig() },
                        enabled = uiState.isConnected && uiState.config.mode != VibroMode.TEST,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("SEND")
                    }
                }

                Spacer(modifier = Modifier.height(15.dp))
            }
        }
    }
}
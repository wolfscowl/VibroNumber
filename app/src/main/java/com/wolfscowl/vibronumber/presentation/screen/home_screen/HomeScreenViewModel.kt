package com.wolfscowl.vibronumber.presentation.screen.home_screen

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wolfscowl.vibronumber.data.bluetooth.BluetoothRepository
import com.wolfscowl.vibronumber.data.bluetooth.toBTDeviceInfo
import com.wolfscowl.vibronumber.data.datastore.DataStoreRepository
import com.wolfscowl.vibronumber.presentation.model.DigitPatterns
import com.wolfscowl.vibronumber.presentation.model.VibroConfig
import com.wolfscowl.vibronumber.presentation.model.VibroMode
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.sqrt

class HomeScreenViewModel(
    private val bluetoothRepository: BluetoothRepository,
    private val dataStoreRepository: DataStoreRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeState())
    val uiState = _uiState.asStateFlow()

    private var isSimulating = false

    init {
        observeBluetoothHardware()
        observeSettingDataStore()
    }

    @SuppressLint("MissingPermission")
    private fun observeBluetoothHardware() {
        bluetoothRepository.state.onEach { hardwareState ->
            _uiState.update { it.copy(
                isConnected = hardwareState.isConnected,
                connectedDevice = hardwareState.connectedDevice?.toBTDeviceInfo()
            ) }
        }.launchIn(viewModelScope)
    }

    private fun observeSettingDataStore() {
        dataStoreRepository.preferences.onEach { prefs ->
            _uiState.update { it.copy(
                config = it.config.copy(
                    ptsStartHoldFactor = prefs.ptsStartHoldFactor,
                    ptsEndHoldFactor = prefs.ptsEndHoldFactor
                )
            ) }
        }.launchIn(viewModelScope)
    }

    // ── CONFIG UPDATES ───────────────────────────────────────────────────────────────────────────

    fun updateDigit(digit: Int) {
        _uiState.update { it.copy(config = it.config.copy(digit = digit)) }
    }

    fun updateMode(mode: VibroMode) {
        _uiState.update { it.copy(config = it.config.copy(mode = mode)) }
    }

    fun updateDuration(durationMs: Int) {
        _uiState.update { it.copy(config = it.config.copy(durationMs = durationMs)) }
    }

    fun updateIntensity(intensityPct: Int) {
        _uiState.update { it.copy(config = it.config.copy(intensityPct = intensityPct)) }
    }


    // ── SIMULATE  ────────────────────────────────────────────────────────────────────────────────

    fun simulate() {
        if (isSimulating) return

        // Snapshots the current config so user changes during simulation don't interfere
        val configSnapshot = uiState.value.config

        viewModelScope.launch {
            isSimulating = true
            when (configSnapshot.mode) {
                VibroMode.DISCR -> simulateDISCR(configSnapshot)
                VibroMode.ATM -> simulateATM(configSnapshot)
                VibroMode.PTS -> simulatePTS(configSnapshot)
            }
            // Reset grid after simulation
            _uiState.update { it.copy(actorIntensities = List(9) { 0 }) }
            isSimulating = false
        }
    }

    private suspend fun simulateDISCR(config: VibroConfig) {
        val pattern = DigitPatterns.patterns[config.digit] ?: return
        val duration = config.durationMs.toLong()
        val intensity = config.intensityPct

        for (actorIndex in pattern) {
            // 1. Motor ON: Update state exactly once at the start
            _uiState.update { state ->
                val newIntensities = List(9) { i -> if (i == actorIndex) intensity else 0 }
                state.copy(actorIntensities = newIntensities)
            }

            delay(duration)

            // 2. Motor OFF: Update state exactly once after the duration has passed
            _uiState.update { state ->
                state.copy(actorIntensities = List(9) { 0 })
            }
        }
    }

    private suspend fun simulateATM(config: VibroConfig) {
        val pattern = DigitPatterns.patterns[config.digit] ?: return
        val duration = config.durationMs.toLong()
        val soa = (0.32 * config.durationMs + 47.3).toLong()
        val intensity = config.intensityPct

        val frameTimeMs = 10L
        val motorDeadlines = LongArray(9) { 0L }
        var currentTime = 0L
        var nextPatternIndex = 0
        val startTime = System.currentTimeMillis()


        // The loop continues as long as
        // - there are motors left to start
        // - OR there are motors still vibrating (haven't reached their deadline).
        while (nextPatternIndex < pattern.size || motorDeadlines.any { it > currentTime }) {
            currentTime = System.currentTimeMillis() - startTime
            
            // 1. Trigger: Check if it is time to start the next motor in the pattern.
            // If there are
            // - motors left to start
            // - and the time has come to start the next motor
            if (nextPatternIndex < pattern.size && currentTime >= nextPatternIndex * soa) {
                val motorIndex = pattern[nextPatternIndex]
                // Set motor deadline (important for long durations and patterns with duplicate actors like '3')
                motorDeadlines[motorIndex] = currentTime + duration
                nextPatternIndex++
            }

            // 2. State Update: Determine which motors should be active based on their deadlines.
            _uiState.update { state ->
                val newIntensities = List(9) { i ->
                    if (currentTime < motorDeadlines[i]) intensity else 0
                }
                state.copy(actorIntensities = newIntensities)
            }

            // 3. Ticker: Advance time by a small step.
            delay(frameTimeMs)
        }
    }



    private suspend fun simulatePTS(config: VibroConfig) {
        val pattern = DigitPatterns.patterns[config.digit] ?: return
        val targetIntensity = config.intensityPct.toDouble()
        val duration = config.durationMs.toDouble()
        val frameTimeMs = 10L

        // Ensure the first actor vibrates at full intensity for a brief moment
        // to clearly signal the start of the pattern.
        val firstActor = pattern.first()
        _uiState.update { state ->
            val newIntensities = MutableList(9) { 0 }
            newIntensities[firstActor] = targetIntensity.toInt()
            state.copy(actorIntensities = newIntensities)
        }
        delay((config.durationMs * config.ptsStartHoldFactor).toLong())

        // Iterate through each consecutive pair of actors in the pattern to create smooth transitions.
        for (i in 0 until pattern.size - 1) {
            val actor1 = pattern[i]
            val actor2 = pattern[i + 1]
            
            val startTime = System.currentTimeMillis()
            var elapsedTime = 0L

            // Perform a smooth fade between the two current actors for the specified transition duration.
            while (elapsedTime < config.durationMs) {
                val beta = (elapsedTime / duration).coerceIn(0.0, 1.0)

                // Calculate intensities for actor 1 and actor 2 using the constant energy formula
                val a1 = (sqrt(1.0 - beta) * targetIntensity).toInt()
                val a2 = (sqrt(beta) * targetIntensity).toInt()

                _uiState.update { state ->
                    val newIntensities = MutableList(9) { 0 }
                    newIntensities[actor1] = a1
                    newIntensities[actor2] = a2
                    state.copy(actorIntensities = newIntensities)
                }

                delay(frameTimeMs)
                elapsedTime = System.currentTimeMillis() - startTime
            }
        }

        // Ensure the last actor vibrates at full intensity for a brief period
        // to clearly signal the completion of the pattern.
        val lastActor = pattern.last()
        _uiState.update { state ->
            val newIntensities = MutableList(9) { 0 }
            newIntensities[lastActor] = targetIntensity.toInt()
            state.copy(actorIntensities = newIntensities)
        }
        delay((config.durationMs * config.ptsEndHoldFactor).toLong())
    }

    // ── DATA TRANSMISSION ────────────────────────────────────────────────────────────────────────

    fun sendConfig() {
        val config = uiState.value.config
        // Example of transmission: DIGIT;MODE;DURATION;INTENSITY;START_FACTOR;END_FACTOR
        val message = "${config.digit};${config.mode.abbreviation};${config.durationMs};${config.intensityPct};${config.ptsStartHoldFactor};${config.ptsEndHoldFactor}\n"
        
        if (bluetoothRepository.isBluetoothEnabled()) {
            bluetoothRepository.sendData(message)
            _uiState.update { it.copy(lastSentMessage = "Sent: $message") }
        }
    }
}





//@Deprecated("Replaced by hardware-near loop implementation in simulateATM")
//private suspend fun simulateATMDeprecated(config: VibroConfig) {
//    val pattern = DigitPatterns.patterns[config.digit] ?: return
//    val d = config.durationMs.toDouble()
//    val intensity = config.intensityPct
//    // Stimulus Onset Asynchrony
//    val soa = (0.32 * d + 47.3).toLong()
//
//    // Use coroutineScope to coordinate overlapping vibrations
//    coroutineScope {
//        for (actorIndex in pattern) {
//            launch {
//                // Motor ON
//                _uiState.update { state ->
//                    val newIntensities = state.actorIntensities.toMutableList()
//                    newIntensities[actorIndex] = intensity
//                    state.copy(actorIntensities = newIntensities)
//                }
//
//                delay(config.durationMs.toLong())
//
//                // Motor OFF
//                _uiState.update { state ->
//                    val newIntensities = state.actorIntensities.toMutableList()
//                    newIntensities[actorIndex] = 0
//                    state.copy(actorIntensities = newIntensities)
//                }
//            }
//            // Wait for SOA before starting the next motor in the pattern
//            delay(soa)
//        }
//    }
//}
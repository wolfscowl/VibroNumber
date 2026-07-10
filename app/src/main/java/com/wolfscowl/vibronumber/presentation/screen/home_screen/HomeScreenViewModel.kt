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
import kotlinx.coroutines.Dispatchers
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
                    ptsEndHoldFactor = prefs.ptsEndHoldFactor,
                    atmStartHoldFactor = prefs.atmStartHoldFactor,
                    atmEndHoldFactor = prefs.atmEndHoldFactor,
                    dscrStartHoldFactor = prefs.dscrStartHoldFactor,
                    dscrEndHoldFactor = prefs.dscrEndHoldFactor,
                    postDigitDelayMs = prefs.postDigitDelayMs
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


    // ── ACTOR TEST ───────────────────────────────────────────────────────────────────────────────

    fun onActorClick(actorIndex: Int) {
        if (isSimulating) return
        // digit is not really the actor index
        val digitSnapshot = _uiState.value.config.digit
        updateDigit(actorIndex)
        sendConfig()
        simulate()
        // reset the digit
        updateDigit(digitSnapshot)
    }


    // ── SIMULATE  ────────────────────────────────────────────────────────────────────────────────

    fun simulate() {
        if (isSimulating) return

        // Snapshots the current config so user changes during simulation don't interfere
        val configSnapshot = uiState.value.config

        viewModelScope.launch {
            isSimulating = true
            when (configSnapshot.mode) {
                VibroMode.TEST -> simulateActorTest(configSnapshot)
                VibroMode.DISCR -> simulateDISCR(configSnapshot)
                VibroMode.ATM -> simulateATM(configSnapshot)
                VibroMode.PTS -> simulatePTS(configSnapshot)
            }
            // Reset grid after simulation
            _uiState.update { it.copy(actorIntensities = List(9) { 0 }) }
            isSimulating = false
        }
    }

    private suspend fun simulateActorTest(config: VibroConfig) {
        val actorIndex = config.digit
        val duration = config.durationMs.toLong()
        val intensity = config.intensityPct

        _uiState.update { state ->
            state.copy(actorIntensities = List(9) { i -> if (i == actorIndex) intensity else 0 })
        }
        delay(duration)
        _uiState.update { state ->
            state.copy(actorIntensities = List(9) { 0 })
        }
    }

    private suspend fun simulateDISCR(config: VibroConfig) {
        val pattern = DigitPatterns.patterns[config.digit] ?: return
        val duration = config.durationMs.toLong()
        val intensity = config.intensityPct
        val startHoldFactor = config.dscrStartHoldFactor
        val endHoldFactor = config.dscrStartHoldFactor

        for (i in pattern.indices) {
            val actorIndex = pattern[i]
            
            // Use factors for first and last actor
            val factor = when (i) {
                0 -> startHoldFactor
                pattern.size - 1 -> endHoldFactor
                else -> 1.0f
            }
            val individualDuration = (duration * factor).toLong()

            // 1. Motor ON
            _uiState.update { state ->
                val newIntensities = List(9) { idx -> if (idx == actorIndex) intensity else 0 }
                state.copy(actorIntensities = newIntensities)
            }

            delay(individualDuration)

            // 2. Motor OFF
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
        val startHoldFactor = config.atmStartHoldFactor
        val endHoldFactor = config.atmStartHoldFactor

        val frameTimeMs = 5L
        val motorDeadlines = LongArray(9) { 0L }
        var currentTime = 0L
        var nextPatternIndex = 0
        val startTime = System.currentTimeMillis()

        // Calculate the lead-in delay from the start factor.
        // If factor is 2.0, the "motion" starts after 1x duration.
        val startExtraDelay = ((startHoldFactor - 1.0f) * duration).toLong().coerceAtLeast(0L)

        while (nextPatternIndex < pattern.size || motorDeadlines.any { it > currentTime }) {
            currentTime = System.currentTimeMillis() - startTime

            // 1. Trigger: Check if it is time to start the next motor in the pattern.
            if (nextPatternIndex < pattern.size) {
                val targetOnsetTime = if (nextPatternIndex == 0) {
                    0L
                } else {
                    startExtraDelay + (nextPatternIndex * soa)
                }

                if (currentTime >= targetOnsetTime) {
                    val motorIndex = pattern[nextPatternIndex]
                    
                    val factor = when (nextPatternIndex) {
                        0 -> startHoldFactor
                        pattern.size - 1 -> endHoldFactor
                        else -> 1.0f
                    }
                    
                    motorDeadlines[motorIndex] = currentTime + (duration * factor).toLong()
                    nextPatternIndex++
                }
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



    private suspend fun simulateATM2(config: VibroConfig) {
        val pattern = DigitPatterns.patterns[config.digit] ?: return
        val duration = config.durationMs.toLong()
        val soa = (0.32 * config.durationMs + 47.3).toLong()
        val intensity = config.intensityPct

        val frameTimeMs = 5L
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
            if (nextPatternIndex < pattern.size && currentTime >= nextPatternIndex * soa) {
                val motorIndex = pattern[nextPatternIndex]

                // Use individual factors for first and last actor
                val factor = when (nextPatternIndex) {
                    0 -> config.atmStartHoldFactor              // First Actor
                    pattern.size - 1 -> config.atmEndHoldFactor // Last Actor
                    else -> 1.0f                                // All Other Actors
                }

                // Set motor deadline based on individual duration
                motorDeadlines[motorIndex] = currentTime + (duration * factor).toLong()
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
        val startHoldFactor = config.atmStartHoldFactor
        val endHoldFactor = config.atmStartHoldFactor
        val frameTimeMs = 5L

        // Ensure the first actor vibrates at full intensity for a brief moment
        // to clearly signal the start of the pattern.
        val firstActor = pattern.first()
        _uiState.update { state ->
            val newIntensities = MutableList(9) { 0 }
            newIntensities[firstActor] = targetIntensity.toInt()
            state.copy(actorIntensities = newIntensities)
        }
        delay((config.durationMs * startHoldFactor).toLong())

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
        delay((config.durationMs * endHoldFactor).toLong())
    }

    // ── DATA TRANSMISSION ────────────────────────────────────────────────────────────────────────

    fun sendConfig() {
        val config = uiState.value.config
        viewModelScope.launch(Dispatchers.IO) {
            // Example of transmission: DIGIT;MODE;DURATION;INTENSITY;PTS_START;PTS_END;ATM_START;ATM_END;DSCR_START;DSCR_END;POST_DELAY
            val message = "${config.digit};${config.mode.abbreviation};${config.durationMs};${config.intensityPct};${config.postDigitDelayMs};${config.dscrStartHoldFactor};${config.dscrEndHoldFactor};${config.atmStartHoldFactor};${config.atmEndHoldFactor};${config.ptsStartHoldFactor};${config.ptsEndHoldFactor}\n"

            if (bluetoothRepository.isBluetoothEnabled()) {
                bluetoothRepository.sendData(message)
                _uiState.update { it.copy(lastSentMessage = "Sent: $message") }
            }
        }
    }
}

package com.wolfscowl.vibronumber.presentation.model

enum class VibroMode(val label: String, val abbreviation: String) {
    TEST("Actor Test", "TEST"),
    DISCR("Discrete Motion", "DISCR"),
    ATM("Apparent Tactile Motion", "ATM"),
    PTS("Phantom Tactile Sensation", "PTS")
}

data class VibroConfig(
    val digit: Int = 0,
    val mode: VibroMode = VibroMode.ATM,
    val durationMs: Int = 300,
    val intensityPct: Int = 50,
    val postDigitDelayMs: Int = 0,
    // Factors relative to durationMs (1.0 to 4.0)
    val dscrStartHoldFactor: Float = 1.0f,
    val dscrEndHoldFactor: Float = 1.0f,
    // Factors relative to durationMs (1.0 to 4.0)
    val atmStartHoldFactor: Float = 1.0f,
    val atmEndHoldFactor: Float = 1.0f,
    // Factors relative to durationMs (0.0 to 3.0)
    val ptsStartHoldFactor: Float = 0.0f,
    val ptsEndHoldFactor: Float = 0.0f,
)

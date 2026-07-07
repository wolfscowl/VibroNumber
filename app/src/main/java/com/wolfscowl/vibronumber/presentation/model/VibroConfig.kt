package com.wolfscowl.vibronumber.presentation.model

enum class VibroMode(val label: String, val abbreviation: String) {
    DISCR("Discrete Motion", "DISCR"),
    ATM("Apparent Tactile Motion", "ATM"),
    PTS("Phantom Tactile Sensation", "PTS")
}

data class VibroConfig(
    val digit: Int = 0,
    val mode: VibroMode = VibroMode.ATM,
    val durationMs: Int = 300,
    val intensityPct: Int = 50,
    // Factors relative to durationMs (0.0 to 2.0)
    val ptsStartHoldFactor: Float = 0.0f,
    val ptsEndHoldFactor: Float = 0.0f
)

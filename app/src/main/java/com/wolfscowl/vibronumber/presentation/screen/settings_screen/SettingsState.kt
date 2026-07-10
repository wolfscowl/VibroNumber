package com.wolfscowl.vibronumber.presentation.screen.settings_screen

data class SettingsState(
    val postDigitDelayMs: Int = 0,
    val dscrStartHoldFactor: Float = 1.0f,
    val dscrEndHoldFactor: Float = 1.0f,
    val atmStartHoldFactor: Float = 1.0f,
    val atmEndHoldFactor: Float = 1.0f,
    val ptsStartHoldFactor: Float = 0.0f,
    val ptsEndHoldFactor: Float = 0.0f
)

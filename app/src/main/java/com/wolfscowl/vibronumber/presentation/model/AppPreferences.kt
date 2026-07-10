package com.wolfscowl.vibronumber.presentation.model

data class AppPreferences(
    val ptsStartHoldFactor: Float = 0.0f,
    val ptsEndHoldFactor: Float = 0.0f,
    val atmStartHoldFactor: Float = 1.0f,
    val atmEndHoldFactor: Float = 1.0f,
    val dscrStartHoldFactor: Float = 1.0f,
    val dscrEndHoldFactor: Float = 1.0f,
    val postDigitDelayMs: Int = 0
)

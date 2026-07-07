package com.wolfscowl.vibronumber.presentation.screen.home_screen

import com.wolfscowl.vibronumber.presentation.model.BTDeviceInfo
import com.wolfscowl.vibronumber.presentation.model.VibroConfig

data class HomeState(
    val isConnected: Boolean = false,
    val connectedDevice: BTDeviceInfo? = null,
    val lastSentMessage: String = "",
    val config: VibroConfig = VibroConfig(),
    val actorIntensities: List<Int> = List(9) { 0 }
)

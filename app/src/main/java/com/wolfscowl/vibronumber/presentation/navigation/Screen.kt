package com.wolfscowl.vibronumber.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector


enum class Screen(
    val label: String,
    val route: String,
    val icon: ImageVector,
) {
    HOME(label = "Home", route = "home_screen", icon = Icons.Outlined.Home),
    BLUETOOTH(label = "Bluetooth", route = "bluetooth_screen", icon = Icons.Outlined.Bluetooth),
    SETTINGS(label = "Settings", route = "setting_screen", icon = Icons.Outlined.Settings)
}
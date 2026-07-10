package com.wolfscowl.vibronumber.presentation.screen.bluettoth_screen

import com.wolfscowl.vibronumber.presentation.model.BTDeviceInfo

data class BluetoothState(
    val isConnected: Boolean = false,
    val connectedDevice: BTDeviceInfo? = null,
    val isScanning: Boolean = false,
    val scannedDevices: List<BTDeviceInfo> = emptyList(),
    val pairedDevices: List<BTDeviceInfo> = emptyList(),
    val isBluetoothEnabled: Boolean = true,
    val isBluetoothAvailable: Boolean = true,
    val hasPermission: Boolean = false,
    val noDevicesFound: Boolean = false
)

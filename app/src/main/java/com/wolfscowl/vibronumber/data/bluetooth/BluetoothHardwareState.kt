package com.wolfscowl.vibronumber.data.bluetooth

import android.bluetooth.BluetoothDevice

data class BluetoothHardwareState(
    val scannedDevices: Set<BluetoothDevice> = emptySet(),
    val pairedDevices: Set<BluetoothDevice> = emptySet(),
    val isScanning: Boolean = false,
    val isConnected: Boolean = false,
    val connectedDevice: BluetoothDevice? = null,
    val isEnabled: Boolean = false,
    val noDevicesFound: Boolean = false
)

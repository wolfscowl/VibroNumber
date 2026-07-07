package com.wolfscowl.vibronumber.data.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import com.wolfscowl.vibronumber.presentation.model.BTDeviceInfo

@SuppressLint("MissingPermission")
fun BluetoothDevice.toBTDeviceInfo(): BTDeviceInfo {
    return BTDeviceInfo(
        name = name ?: "Unknown Device",
        address = address
    )
}

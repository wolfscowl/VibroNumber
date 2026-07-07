package com.wolfscowl.vibronumber.data.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BluetoothDeviceReceiver(
    private val onDeviceFound: (BluetoothDevice) -> Unit,
    private val onDiscoveryFinished: () -> Unit
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        when(intent?.action) {
            BluetoothDevice.ACTION_FOUND -> {
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                device?.let { onDeviceFound(it) }
            }
            BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                onDiscoveryFinished() // Neuer Callback
            }
        }
    }
}

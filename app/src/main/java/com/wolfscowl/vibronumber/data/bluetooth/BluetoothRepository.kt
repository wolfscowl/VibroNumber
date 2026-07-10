package com.wolfscowl.vibronumber.data.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.IntentFilter
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.UUID


@SuppressLint("MissingPermission")
class BluetoothRepository(private val context: Context) {

    // ── BLUETOOTH ADAPTER ────────────────────────────────────────────────────────────────────────
    private val bluetoothManager: BluetoothManager? =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter


    // ── BLUETOOTH HARDWARE STATE ─────────────────────────────────────────────────────────────────
    private val _state = MutableStateFlow(
        BluetoothHardwareState(isEnabled = isBluetoothEnabled())
    )
    val state = _state.asStateFlow()

    private var currentSocket: BluetoothSocket? = null
    
    // Standard UUID for Serial Port Profile (SPP)
    private val SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")


    // ── BROADCAST RECEIVER - BT STATE ────────────────────────────────────────────────────────────
    private val stateReceiver = BluetoothStateReceiver { isEnabled ->
        _state.update { it.copy(isEnabled = isEnabled, noDevicesFound = false) }
        if (!isEnabled) {
            _state.update { 
                it.copy(
                    isScanning = false, 
                    scannedDevices = emptySet(), 
                    pairedDevices = emptySet(),
                    isConnected = false 
                ) 
            }
            closeConnection()
        } else {
            updatePairedDevices()
        }
    }


    // ── BROADCAST RECEIVER - BT DEVICE ───────────────────────────────────────────────────────────
    private val deviceReceiver = BluetoothDeviceReceiver(
        onDeviceFound = { device ->
            _state.update { it.copy(scannedDevices = it.scannedDevices + device) }
        },
        onDiscoveryFinished = {
            _state.update { 
                it.copy(
                    isScanning = false,
                    noDevicesFound = it.scannedDevices.isEmpty()
                ) 
            }
        }
    )


    init {
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        context.registerReceiver(stateReceiver, filter)
        updatePairedDevices()
    }



    // ── FUNCTIONS ────────────────────────────────────────────────────────────────────────────────
    fun isBluetoothAvailable(): Boolean = bluetoothAdapter != null

    /**
     * Checks only if the hardware is enabled.
     * Note: This may still throw SecurityException on Android 12+ if permissions are missing.
     */
    fun isBluetoothEnabled(): Boolean {
        if (bluetoothAdapter == null) return false
        
        return try {
            bluetoothAdapter.isEnabled
        } catch (e: SecurityException) {
            Log.e("BluetoothRepo", "SecurityException during isEnabled check. Permission missing?", e)
            false
        }
    }


    /**
     * Updates the list of paired devices. 
     * Expects permissions to be granted.
     */
    fun updatePairedDevices() {
        if (!isBluetoothEnabled()) return
        
        try {
            val bondedDevices = bluetoothAdapter?.bondedDevices ?: emptySet()
            _state.update { it.copy(pairedDevices = bondedDevices) }
        } catch (e: SecurityException) {
            Log.e("BluetoothRepo", "SecurityException during bondedDevices access", e)
        }
    }


    fun startDiscovery() {
        if (!isBluetoothEnabled()) return
        
        _state.update { it.copy(isScanning = true, scannedDevices = emptySet(), noDevicesFound = false) }
        updatePairedDevices() 
        
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        context.registerReceiver(deviceReceiver, filter)
        
        bluetoothAdapter?.startDiscovery()
    }

    fun stopDiscovery() {
        bluetoothAdapter?.cancelDiscovery()
        _state.update { it.copy(isScanning = false) }
        try {
            context.unregisterReceiver(deviceReceiver)
        } catch (e: IllegalArgumentException) {
            // Receiver was not registered
        }
    }


    suspend fun connectToDevice(device: BluetoothDevice) {
        withContext(Dispatchers.IO) {
            try {
                // RFCOMM socket creation for Classic Bluetooth
                currentSocket = device.createRfcommSocketToServiceRecord(SPP_UUID)
                currentSocket?.connect()
                _state.update { it.copy(isConnected = true, connectedDevice = device) }
                Log.d("BluetoothRepo", "Connected to ${device.name}")
            } catch (e: IOException) {
                Log.e("BluetoothRepo", "Connection failed", e)
                closeConnection()
            } catch (e: SecurityException) {
                Log.e("BluetoothRepo", "SecurityException during connect", e)
            }
        }
    }


    fun closeConnection() {
        try {
            currentSocket?.close()
        } catch (e: IOException) {
            Log.e("BluetoothRepo", "Could not close socket", e)
        }
        currentSocket = null
        _state.update { it.copy(isConnected = false, connectedDevice = null) }
    }



    fun sendData(data: String) {
        val socket = currentSocket ?: return
        if (!_state.value.isConnected) return
        
        try {
            socket.outputStream.write(data.toByteArray())
            Log.d("BluetoothRepo", "Sent data: $data")
        } catch (e: IOException) {
            Log.e("BluetoothRepo", "Failed to send data", e)
            closeConnection()
        }
    }
}

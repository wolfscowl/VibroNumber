package com.wolfscowl.vibronumber.presentation.screen.bluettoth_screen

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wolfscowl.vibronumber.data.bluetooth.BluetoothRepository
import com.wolfscowl.vibronumber.data.bluetooth.toBTDeviceInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BluetoothViewModel(
    private val bluetoothRepository: BluetoothRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(BluetoothState())
    val uiState = _uiState.asStateFlow()

    private var deviceMap = mapOf<String, BluetoothDevice>()

    init {
        observeBluetoothHardware()
    }

    @SuppressLint("MissingPermission")
    private fun observeBluetoothHardware() {
        bluetoothRepository.state
            .onEach { hardwareState ->
                // Update the private device mapping for establishing a connection
                deviceMap = (hardwareState.pairedDevices + hardwareState.scannedDevices)
                    .associateBy { it.address }

                _uiState.update { it.copy(
                    scannedDevices = hardwareState.scannedDevices.map { it.toBTDeviceInfo() },
                    pairedDevices = hardwareState.pairedDevices.map { it.toBTDeviceInfo() },
                    isScanning = hardwareState.isScanning,
                    isConnected = hardwareState.isConnected,
                    connectedDevice = hardwareState.connectedDevice?.toBTDeviceInfo(),
                    isBluetoothEnabled = hardwareState.isEnabled,
                    isBluetoothAvailable = bluetoothRepository.isBluetoothAvailable(),
                    noDevicesFound = hardwareState.noDevicesFound
                ) }
            }.launchIn(viewModelScope)
    }

    fun startScan() {
        bluetoothRepository.startDiscovery()
    }

    fun stopScan() {
        bluetoothRepository.stopDiscovery()
    }

    fun updatePairedDevices() {
        bluetoothRepository.updatePairedDevices()
    }

    fun connectToDevice(address: String) {
        val device = deviceMap[address] ?: return
        viewModelScope.launch {
            bluetoothRepository.connectToDevice(device)
        }
    }

    fun disconnect() {
        bluetoothRepository.closeConnection()
    }

    // Permission state is handled by the UI layer
    fun setPermissionGranted(isGranted: Boolean) {
        _uiState.update { it.copy(hasPermission = isGranted) }
        if (isGranted) {
            updatePairedDevices()
        }
    }
}

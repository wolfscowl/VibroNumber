package com.wolfscowl.vibronumber.presentation.screen.bluettoth_screen

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wolfscowl.vibronumber.presentation.app.AppViewModelProvider
import com.wolfscowl.vibronumber.presentation.commons.UIDesign

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BluetoothScreen(
    viewModel: BluetoothViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // ── INITIAL PERMISSION CHECK ──────────────────────────────────────────────────────────────
    LaunchedEffect(Unit) {
        val result = getRequiredBluetoothPermissions().all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        viewModel.setPermissionGranted(result)
    }

    // ── PERMISSION LAUNCHER ──────────────────────────────────────────────────────────────────────
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        viewModel.setPermissionGranted(allGranted)
//        if (allGranted) {
//            viewModel.startScan()
//        }
    }


    // ── SCAFFOLD ─────────────────────────────────────────────────────────────────────────────────
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bluetooth") },
                actions = {
                    if (uiState.isScanning) {
                        Text(
                            text = "Scanning",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                    IconButton(
                        onClick = {
                            if (uiState.isScanning) {
                                viewModel.stopScan()
                            } else {
                                viewModel.startScan()
                            }
                        },
                        enabled = uiState.isBluetoothEnabled && uiState.isBluetoothAvailable && uiState.hasPermission
                    ) {
                        if (uiState.isScanning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Start Scan"
                            )
                        }
                    }
                },
                colors = UIDesign.topAppBarColors
            )
        }
    ) { innerPadding ->

        // ── LAYOUT ───────────────────────────────────────────────────────────────────────────────
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 16.dp)
        ) {
            // ── HINT MESSAGES ────────────────────────────────────────────────────────────────────
            if (!uiState.isBluetoothAvailable) {
                Text(
                    text = "Bluetooth is not supported on this device!",
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                )
            } else if (!uiState.hasPermission) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Bluetooth permissions are required!",
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { permissionLauncher.launch(getRequiredBluetoothPermissions()) }
                    ) {
                        Text("Grant Permissions")
                    }
                }
            } else if (!uiState.isBluetoothEnabled) {
                Text(
                    text = "Please enable Bluetooth to scan for devices!",
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                )
            }

            // ── BLUETOOTH DEVICES LIST ───────────────────────────────────────────────────────────────
            LazyColumn(modifier = Modifier.fillMaxSize()) {

                // ── 1. Active Connection Section ────────────────────────────────────────────────────────
                if (uiState.isConnected && uiState.connectedDevice != null) {
                    item {
                        Text(
                            text = "Active Connection",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            ListItem(
                                headlineContent = {
                                    Text(
                                        uiState.connectedDevice!!.name,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                supportingContent = { Text(uiState.connectedDevice!!.address) },
                                leadingContent = {
                                    Icon(
                                        imageVector = Icons.Default.BluetoothConnected,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                trailingContent = {
                                    Text(
                                        modifier = Modifier.clickable { viewModel.disconnect() },
                                        text = "disconnect",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                colors = androidx.compose.material3.ListItemDefaults.colors(
                                    containerColor = Color.Transparent
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }

                // ── 2. Scanned Devices Section ──────────────────────────────────────────────────────
                if (uiState.isScanning || uiState.scannedDevices.isNotEmpty() || uiState.noDevicesFound) {
                    item {
                        Text(
                            text = "Available Devices",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
                if (uiState.scannedDevices.isNotEmpty()) {
                    items(uiState.scannedDevices) { deviceInfo ->
                        val isConnected =
                            uiState.isConnected && uiState.connectedDevice?.address == deviceInfo.address
                        ListItem(
                            headlineContent = { Text(deviceInfo.name) },
                            supportingContent = { Text(deviceInfo.address) },
                            leadingContent = {
                                Icon(
                                    imageVector = if (isConnected) Icons.Default.BluetoothConnected else Icons.Default.Bluetooth,
                                    contentDescription = null,
                                    tint = if (isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            modifier = Modifier.clickable { viewModel.connectToDevice(deviceInfo.address) }
                        )
                        HorizontalDivider()
                    }
                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
                // ── No Device Found Message ──────────────────────────────────────────────────────
                if (!uiState.isScanning && uiState.noDevicesFound && uiState.scannedDevices.isEmpty()) {
                    item {
                        Text(
                            text = "No new devices found in range",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp)
                        )
                    }
                }

                // ── 3. Paired Devices Section ───────────────────────────────────────────────────────
                if (uiState.pairedDevices.isNotEmpty()) {
                    item {
                        Text(
                            text = "Paired Devices",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(uiState.pairedDevices) { deviceInfo ->
                        val isConnected =
                            uiState.isConnected && uiState.connectedDevice?.address == deviceInfo.address
                        ListItem(
                            headlineContent = { Text(deviceInfo.name) },
                            supportingContent = { Text(deviceInfo.address) },
                            leadingContent = {
                                Icon(
                                    imageVector = if (isConnected) Icons.Default.BluetoothConnected else Icons.Default.Bluetooth,
                                    contentDescription = null,
                                    tint = if (isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            modifier = Modifier.clickable { viewModel.connectToDevice(deviceInfo.address) }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}


private fun getRequiredBluetoothPermissions(): Array<String> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
    } else {
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }
}

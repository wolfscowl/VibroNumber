package com.wolfscowl.vibronumber.presentation.app

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.wolfscowl.vibronumber.VibroNumberApplication
import com.wolfscowl.vibronumber.presentation.screen.bluettoth_screen.BluetoothViewModel
import com.wolfscowl.vibronumber.presentation.screen.home_screen.HomeScreenViewModel
import com.wolfscowl.vibronumber.presentation.screen.settings_screen.SettingsViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            BluetoothViewModel(
                bluetoothRepository = vibroApplication().bluetoothRepository
            )
        }

        initializer {
            HomeScreenViewModel(
                bluetoothRepository = vibroApplication().bluetoothRepository,
                dataStoreRepository = vibroApplication().dataStoreRepository
            )
        }

        initializer {
            SettingsViewModel(
                dataStoreRepository = vibroApplication().dataStoreRepository
            )
        }
    }
}

// Provide the VibroNumberApplication
fun CreationExtras.vibroApplication(): VibroNumberApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as VibroNumberApplication)

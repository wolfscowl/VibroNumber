package com.wolfscowl.vibronumber

import android.app.Application
import com.wolfscowl.vibronumber.data.bluetooth.BluetoothRepository
import com.wolfscowl.vibronumber.data.datastore.DataStoreRepository

class VibroNumberApplication : Application() {
    
    // Lazy initialization of the repositories
    val bluetoothRepository: BluetoothRepository by lazy {
        BluetoothRepository(applicationContext)
    }

    val dataStoreRepository: DataStoreRepository by lazy {
        DataStoreRepository(applicationContext)
    }
}

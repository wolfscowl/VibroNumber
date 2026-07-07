package com.wolfscowl.vibronumber.presentation.screen.settings_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wolfscowl.vibronumber.data.datastore.DataStoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val dataStoreRepository: DataStoreRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsState())
    val uiState = _uiState.asStateFlow()

    init {
        observeDataStore()
    }


    private fun observeDataStore() {
        dataStoreRepository.preferences.onEach { prefs ->
            _uiState.update {
                it.copy(
                    ptsStartHoldFactor = prefs.ptsStartHoldFactor,
                    ptsEndHoldFactor = prefs.ptsEndHoldFactor
                )
            }
        }.launchIn(viewModelScope)
    }

    fun updateStartHoldFactor(factor: Float) {
        viewModelScope.launch {
            dataStoreRepository.savePtsStartHoldFactor(factor)
        }
    }

    fun updateEndHoldFactor(factor: Float) {
        viewModelScope.launch {
            dataStoreRepository.savePtsEndHoldFactor(factor)
        }
    }
}

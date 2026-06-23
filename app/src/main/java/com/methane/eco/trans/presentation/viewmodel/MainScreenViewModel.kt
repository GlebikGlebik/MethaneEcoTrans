package com.methane.eco.trans.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.methane.eco.trans.presentation.enterscreen.EnterScreenUiState
import com.methane.eco.trans.presentation.mainscreen.MainScreenEvent
import com.methane.eco.trans.presentation.mainscreen.MainScreenUIState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

class MainScreenViewModel(): ViewModel() {
    private val _uiState = MutableStateFlow(MainScreenUIState())

    val uiState: StateFlow<MainScreenUIState> = _uiState.asStateFlow()
    private val _events = Channel<MainScreenEvent>()

    val events = _events.receiveAsFlow()

    fun onDateChanged(newDate: String){
        _uiState.value = _uiState.value.copy(
            date = newDate
        )
    }

    fun onVolumeChanged(newVolume: String){
        _uiState.value = _uiState.value.copy(
            volume = newVolume
        )
    }

    fun onSumChanged(newSum: String){
        _uiState.value = _uiState.value.copy(
            sum = newSum
        )
    }

    fun onUserVehiclesChanged(newUserVehicles: List<String>){
        _uiState.value = _uiState.value.copy(
            userVehicles = newUserVehicles
        )
    }

    fun newVehicleChanged(superNewVehicle: String){
        _uiState.value = _uiState.value.copy(
            newVehicle = superNewVehicle
        )
    }

    fun currentVehicleChanged(newCurrentVehicle: String){
        _uiState.value = _uiState.value.copy(
            currentVehicle = newCurrentVehicle
        )
    }

    fun onShowRefuelDialogChanged(newShowRefuelDialog: Boolean ){
        _uiState.value = _uiState.value.copy(
            showRefuelDialog = newShowRefuelDialog
        )
    }

    fun isLoadingChanged(newIsLoading: Boolean){
        _uiState.value = _uiState.value.copy(
            isLoading = newIsLoading
        )
    }



}
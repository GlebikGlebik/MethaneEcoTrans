package com.methane.eco.trans.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.methane.eco.trans.data.dto.RefuelingDto
import com.methane.eco.trans.data.dto.VehicleDto
import com.methane.eco.trans.domain.usecase.AddRefuelingUseCase
import com.methane.eco.trans.domain.usecase.AddVehicleUseCase
import com.methane.eco.trans.domain.usecase.DeleteVehicleUseCase
import com.methane.eco.trans.domain.usecase.GetRefuelingHistoryUseCase
import com.methane.eco.trans.domain.usecase.GetVehiclesUseCase
import com.methane.eco.trans.presentation.mainscreen.MainScreenEvent
import com.methane.eco.trans.presentation.mainscreen.MainScreenUIState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class MainScreenViewModel(
    private val getVehiclesUseCase: GetVehiclesUseCase,
    private val addVehicleUseCase: AddVehicleUseCase,
    private val deleteVehicleUseCase: DeleteVehicleUseCase,
    private val addRefuelingUseCase: AddRefuelingUseCase,
    private val getRefuelingHistoryUseCase: GetRefuelingHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainScreenUIState())
    val uiState: StateFlow<MainScreenUIState> = _uiState.asStateFlow()

    private val _events = Channel<MainScreenEvent>()
    val events = _events.receiveAsFlow()

    init {
        loadVehicles()
    }

    // Сеттеры
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

    fun onUserVehiclesChanged(newUserVehicles: List<VehicleDto>){
        _uiState.value = _uiState.value.copy(
            userVehicles = newUserVehicles
        )
    }

    fun onNewVehicleChanged(newVehicle: String) {
        _uiState.value = _uiState.value.copy(
            newVehicle = newVehicle)
    }

    fun onCurrentVehicleChanged(newCurrentVehicle: String) {
        _uiState.value = _uiState.value.copy(
            currentVehicle = newCurrentVehicle)
    }

    fun onShowRefuelDialogChanged(newShowRefuelDialog: Boolean ){
        _uiState.value = _uiState.value.copy(
            showRefuelDialog = newShowRefuelDialog
        )
    }

    fun onProfileClicked() {
        viewModelScope.launch {
            _events.send(MainScreenEvent.NavigateToProfileScreen)
        }
    }

    fun onHistoryClicked() {
        viewModelScope.launch {
            _events.send(MainScreenEvent.NavigateToHistoryScreen)
        }
    }


    // Загрузка списка ТС
    fun loadVehicles() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            getVehiclesUseCase().fold(
                onSuccess = { vehicles ->
                    _uiState.value = _uiState.value.copy(
                        userVehicles = vehicles,
                        isLoading = false
                    )
                },
                onFailure = { error ->
                    _events.send(MainScreenEvent.ShowSnackbar("Ошибка загрузки ТС: ${error.message}"))
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            )
        }
    }

    // Добавление ТС
    fun addVehicle(
        name: String,
        licensePlate: String?,
        vinNumber: String,
        model: String,
        year: Int?,
        mileage: Double?
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            addVehicleUseCase(name, licensePlate, vinNumber, model, year, mileage).fold(
                onSuccess = { message ->
                    _events.send(MainScreenEvent.ShowSnackbar(message))
                    loadVehicles() // Перезагружаем список
                    _uiState.value = _uiState.value.copy(
                        newVehicle = "",
                        isLoading = false
                    )
                },
                onFailure = { error ->
                    _events.send(MainScreenEvent.ShowSnackbar("Ошибка: ${error.message}"))
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            )
        }
    }

    // Удаление ТС
    fun deleteVehicle(vehicleId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            deleteVehicleUseCase(vehicleId).fold(
                onSuccess = {
                    _events.send(MainScreenEvent.ShowSnackbar("ТС удалено"))
                    loadVehicles()
                    _uiState.value = _uiState.value.copy(isLoading = false)
                },
                onFailure = { error ->
                    _events.send(MainScreenEvent.ShowSnackbar("Ошибка: ${error.message}"))
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            )
        }
    }

    // Добавление заправки
    fun addRefueling(
        vehicleId: String,
        volume: Double,
        totalSum: Double,
        refuelDate: String
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            addRefuelingUseCase(vehicleId, volume, totalSum, refuelDate).fold(
                onSuccess = { message ->
                    _events.send(MainScreenEvent.ShowSnackbar(message))
                    _uiState.value = _uiState.value.copy(
                        date = "",
                        volume = "",
                        sum = "",
                        currentVehicle = "",
                        isLoading = false
                    )
                },
                onFailure = { error ->
                    _events.send(MainScreenEvent.ShowSnackbar("Ошибка: ${error.message}"))
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            )
        }
    }
}
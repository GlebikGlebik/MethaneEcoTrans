package com.methane.eco.trans.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.methane.eco.trans.domain.usecase.GetRefuelingHistoryUseCase
import com.methane.eco.trans.domain.usecase.GetVehiclesUseCase
import com.methane.eco.trans.presentation.historyscreen.HistoryScreenEvent
import com.methane.eco.trans.presentation.historyscreen.HistoryScreenUIState
import com.methane.eco.trans.presentation.historyscreen.SortBy
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val getVehiclesUseCase: GetVehiclesUseCase,
    private val getRefuelingHistoryUseCase: GetRefuelingHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryScreenUIState())
    val uiState: StateFlow<HistoryScreenUIState> = _uiState.asStateFlow()

    private val _events = Channel<HistoryScreenEvent>()
    val events = _events.receiveAsFlow()

    init {
        loadVehicles()
        loadHistory() // Загружаем историю сразу при входе на экран
    }

    private fun loadVehicles() {
        viewModelScope.launch {
            getVehiclesUseCase().fold(
                onSuccess = { vehicles ->
                    _uiState.value = _uiState.value.copy(userVehicles = vehicles)
                },
                onFailure = { error ->
                    _events.send(HistoryScreenEvent.ShowSnackbar("Ошибка загрузки ТС: ${error.message}"))
                }
            )
        }
    }

    private fun loadHistory() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val state = _uiState.value

            // Запрашиваем данные с сервера (берем с запасом для клиентской сортировки)
            getRefuelingHistoryUseCase(
                vehicleId = state.selectedVehicleId,
                page = 0,
                size = 100
            ).fold(
                onSuccess = { historyResponse ->
                    var filteredList = historyResponse.refuelings

                    // 1. Клиентская фильтрация по топливной карте
                    if (state.onlyFuelCard) {
                        filteredList = filteredList.filter { it.fuelCardId != null }
                    }

                    // 2. Клиентская сортировка
                    filteredList = when (state.sortBy) {
                        SortBy.DATE -> filteredList.sortedByDescending { it.refuelDate }
                        SortBy.SUM -> filteredList.sortedByDescending { it.totalSum }
                        SortBy.VOLUME -> filteredList.sortedByDescending { it.volume }
                    }

                    _uiState.value = _uiState.value.copy(
                        history = filteredList,
                        isLoading = false
                    )
                },
                onFailure = { error ->
                    _events.send(HistoryScreenEvent.ShowSnackbar("Ошибка загрузки истории: ${error.message}"))
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            )
        }
    }

    // Сеттеры для фильтров
    fun onVehicleFilterChanged(vehicleId: String?) {
        _uiState.value = _uiState.value.copy(selectedVehicleId = vehicleId)
        loadHistory()
    }

    fun onSortByChanged(sortBy: SortBy) {
        _uiState.value = _uiState.value.copy(sortBy = sortBy)
        loadHistory()
    }

    fun onOnlyFuelCardChanged(only: Boolean) {
        _uiState.value = _uiState.value.copy(onlyFuelCard = only)
        loadHistory()
    }

    fun onMainClicked() {
        viewModelScope.launch { _events.send(HistoryScreenEvent.NavigateToMainScreen) }
    }

    fun onProfileClicked() {
        viewModelScope.launch { _events.send(HistoryScreenEvent.NavigateToProfileScreen) }
    }
}
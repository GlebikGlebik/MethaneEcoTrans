package com.methane.eco.trans.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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

    // ⚠️ MOCK ДАННЫЕ ДЛЯ СЕРВЕРА
    private val MOCK_GAS_STATION_PRICES_ID = "76aec5a0-f768-4ea4-8639-f04bda007626"

    init {
        loadVehicles()
    }


    // --- Сеттеры для UI ---
    fun onDateChanged(newDate: String) {
        _uiState.value = _uiState.value.copy(date = newDate)
    }

    fun onVolumeChanged(newVolume: String) {
        _uiState.value = _uiState.value.copy(volume = newVolume)
    }

    fun onSumChanged(newSum: String) {
        _uiState.value = _uiState.value.copy(sum = newSum)
    }

    fun onFuelCardChanged(number: String) {
        _uiState.value = _uiState.value.copy(fuelCardNumber = number)
    }

    fun onCurrentVehicleIdChanged(id: String) {
        _uiState.value = _uiState.value.copy(currentVehicleId = id)
    }

    fun onShowRefuelDialogChanged(show: Boolean) {
        _uiState.value = _uiState.value.copy(showRefuelDialog = show)
    }

    // ✅ НОВОЕ: Сеттер для ввода номера нового авто
    fun onNewVehiclePlateChanged(plate: String) {
        if (plate.length <= 15) {
            _uiState.value = _uiState.value.copy(newVehiclePlate = plate)
        }
    }

    // --- Навигация ---
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

    // --- Загрузка ТС ---
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

    // Быстрое добавление ТС
    fun addNewVehicle() {
        val plate = _uiState.value.newVehiclePlate
        if (plate.isBlank()) {
            viewModelScope.launch {
                _events.send(MainScreenEvent.ShowSnackbar("Введите номер авто"))
            }
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Генерируем 17-значный VIN, чтобы сервер не выдал ошибку
            // Формат: MCK + номер (до 14 символов, дополненный X)
            val mockVin = "MCK${plate.padEnd(14, 'X').take(14)}"

            addVehicleUseCase(
                name = plate,
                licensePlate = plate,
                vinNumber = mockVin,
                model = "Unknown",
                year = null,
                mileage = null
            ).fold(
                onSuccess = { message ->
                    _events.send(MainScreenEvent.ShowSnackbar(message))
                    _uiState.value = _uiState.value.copy(
                        newVehiclePlate = "",
                        isLoading = false
                    )
                    loadVehicles() // Перезагружаем список
                },
                onFailure = { error ->
                    _events.send(MainScreenEvent.ShowSnackbar("Ошибка: ${error.message}"))
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            )
        }
    }

    // Удаление ТС по введенному номеру
    fun deleteVehicleByPlate() {
        val plate = _uiState.value.newVehiclePlate
        val vehicleToDelete = _uiState.value.userVehicles.find {
            it.licensePlate == plate || it.name == plate
        }

        if (vehicleToDelete == null) {
            viewModelScope.launch {
                _events.send(MainScreenEvent.ShowSnackbar("Авто с таким номером не найдено"))
            }
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            deleteVehicleUseCase(vehicleToDelete.vehicleId).fold(
                onSuccess = {
                    _events.send(MainScreenEvent.ShowSnackbar("Авто удалено"))
                    _uiState.value = _uiState.value.copy(
                        newVehiclePlate = "",
                        isLoading = false
                    )
                    // Если удалили выбранное авто, сбрасываем выбор
                    if (_uiState.value.currentVehicleId == vehicleToDelete.vehicleId) {
                        _uiState.value = _uiState.value.copy(currentVehicleId = "")
                    }
                    loadVehicles()
                },
                onFailure = { error ->
                    _events.send(MainScreenEvent.ShowSnackbar("Ошибка: ${error.message}"))
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            )
        }
    }

    // --- Добавление заправки ---
    fun addRefueling() {
        val state = _uiState.value

        val isoDate = try {
            val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
            val localDate = LocalDate.parse(state.date, dateFormatter)
            localDate.atStartOfDay().toString() // "2006-06-25T00:00:00"
        } catch (e: Exception) {
            viewModelScope.launch {
                _events.send(MainScreenEvent.ShowSnackbar("Неверный формат даты"))
            }
            _uiState.value = state.copy(isLoading = false)
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true)

            addRefuelingUseCase(
                vehicleId = state.currentVehicleId,
                gasStationPricesId = MOCK_GAS_STATION_PRICES_ID,
                volume = state.volume.toDoubleOrNull() ?: 0.0,
                totalSum = state.sum.toDoubleOrNull() ?: 0.0,
                refuelDate = state.date, // Ожидается ISO формат
                fuelCardId = state.fuelCardNumber.takeIf { it.isNotBlank() }
            ).fold(
                onSuccess = { message ->
                    _events.send(MainScreenEvent.ShowSnackbar(message))
                    _uiState.value = _uiState.value.copy(
                        date = "",
                        volume = "",
                        sum = "",
                        fuelCardNumber = "",
                        currentVehicleId = "",
                        showRefuelDialog = false,
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
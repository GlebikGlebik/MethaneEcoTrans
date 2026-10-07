package com.methane.eco.trans.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.methane.eco.trans.data.dto.RefuelingDto
import com.methane.eco.trans.domain.usecase.AddRefuelingUseCase
import com.methane.eco.trans.domain.usecase.GetRefuelingHistoryUseCase
import com.methane.eco.trans.domain.usecase.GetVehiclesUseCase
import com.methane.eco.trans.isDateValid
import com.methane.eco.trans.presentation.historyscreen.HistoryScreenEvent
import com.methane.eco.trans.presentation.historyscreen.HistoryScreenUIState
import com.methane.eco.trans.presentation.historyscreen.PeriodFilter
import com.methane.eco.trans.presentation.historyscreen.SortBy
import com.methane.eco.trans.presentation.historyscreen.toDateRange
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HistoryViewModel(
    private val getVehiclesUseCase: GetVehiclesUseCase,
    private val getRefuelingHistoryUseCase: GetRefuelingHistoryUseCase,
    private val addRefuelingUseCase: AddRefuelingUseCase // НОВОЕ: нужен для модалки "Добавить заправку"
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryScreenUIState())
    val uiState: StateFlow<HistoryScreenUIState> = _uiState.asStateFlow()

    private val _events = Channel<HistoryScreenEvent>()
    val events = _events.receiveAsFlow()

    // TODO: вынести в общий object-constants файл, когда появится больше одной АЗС.
    private val MOCK_GAS_STATION_PRICES_ID = "76aec5a0-f768-4ea4-8639-f04bda007626"

    private val monthNames = listOf(
        "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
        "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
    )

    init {
        loadVehicles()
        loadHistory()
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
            val (startDate, endDate) = state.periodFilter.toDateRange()

            getRefuelingHistoryUseCase(
                vehicleId = state.selectedVehicleId,
                startDate = startDate,
                endDate = endDate,
                page = 0,
                size = 100
            ).fold(
                onSuccess = { historyResponse ->
                    var filteredList = historyResponse.refuelings

                    if (state.onlyFuelCard) {
                        filteredList = filteredList.filter { it.fuelCardId != null }
                    }

                    filteredList = when (state.sortBy) {
                        SortBy.DATE -> filteredList.sortedByDescending { it.refuelDate }
                        SortBy.SUM -> filteredList.sortedByDescending { it.totalSum }
                        SortBy.VOLUME -> filteredList.sortedByDescending { it.volume }
                    }

                    val summary = calculateCurrentMonthSummary(filteredList)

                    _uiState.value = _uiState.value.copy(
                        history = filteredList,
                        isLoading = false,
                        currentMonthLabel = summary.label,
                        currentMonthTotalSum = summary.totalSum,
                        currentMonthTotalVolume = summary.totalVolume,
                        currentMonthVisits = summary.visits
                        // currentMonthSavings сознательно не трогаем — см. TODO в UIState
                    )
                },
                onFailure = { error ->
                    _events.send(HistoryScreenEvent.ShowSnackbar("Ошибка загрузки истории: ${error.message}"))
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            )
        }
    }

    private data class MonthSummary(
        val label: String,
        val totalSum: Double,
        val totalVolume: Double,
        val visits: Int
    )

    /**
     * Берёт самый свежий месяц из уже загруженной (и отфильтрованной) истории
     * и считает по нему сумму/объём/число визитов — ровно то, что дизайн
     * показывает в карточке "Расходы в <месяце>" над фильтрами.
     */
    private fun calculateCurrentMonthSummary(list: List<RefuelingDto>): MonthSummary {
        if (list.isEmpty()) return MonthSummary("", 0.0, 0.0, 0)

        val latestEntry = list.maxByOrNull { it.refuelDate } ?: return MonthSummary("", 0.0, 0.0, 0)
        val parts = latestEntry.refuelDate.substringBefore('T').split("-")
        val year = parts.getOrNull(0)?.toIntOrNull() ?: return MonthSummary("", 0.0, 0.0, 0)
        val month = parts.getOrNull(1)?.toIntOrNull() ?: return MonthSummary("", 0.0, 0.0, 0)

        val monthEntries = list.filter { entry ->
            val p = entry.refuelDate.substringBefore('T').split("-")
            p.getOrNull(0)?.toIntOrNull() == year && p.getOrNull(1)?.toIntOrNull() == month
        }

        return MonthSummary(
            label = monthNames.getOrElse(month - 1) { "" },
            totalSum = monthEntries.sumOf { it.totalSum },
            totalVolume = monthEntries.sumOf { it.volume },
            visits = monthEntries.size
        )
    }

    // ================== Фильтры ==================

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

    fun onPeriodFilterChanged(period: PeriodFilter) {
        _uiState.value = _uiState.value.copy(periodFilter = period)
        loadHistory()
    }

    // ================== Навигация нижнего меню ==================

    fun onMainClicked() {
        viewModelScope.launch { _events.send(HistoryScreenEvent.NavigateToMainScreen) }
    }

    fun onProfileClicked() {
        viewModelScope.launch { _events.send(HistoryScreenEvent.NavigateToProfileScreen) }
    }

    fun onQrClicked() {
        viewModelScope.launch { _events.send(HistoryScreenEvent.NavigateToQrScreen) }
    }

    fun onMoreClicked() {
        viewModelScope.launch { _events.send(HistoryScreenEvent.NavigateToMoreScreen) }
    }

    // ================== Модалка "Добавить заправку" ==================

    fun onShowAddRefuelSheetChanged(show: Boolean) {
        _uiState.value = if (show) {
            _uiState.value.copy(showAddRefuelSheet = true)
        } else {
            // сбрасываем черновик при закрытии — в следующий раз откроется чистая форма
            _uiState.value.copy(showAddRefuelSheet = false, addDate = "", addVolume = "", addSum = "")
        }
    }

    fun onAddDateChanged(value: String) {
        _uiState.value = _uiState.value.copy(addDate = value)
    }

    fun onAddVolumeChanged(value: String) {
        _uiState.value = _uiState.value.copy(addVolume = value)
    }

    fun onAddSumChanged(value: String) {
        _uiState.value = _uiState.value.copy(addSum = value)
    }

    fun onSubmitAddRefueling() {
        val state = _uiState.value

        if (state.addDate.isBlank() || state.addVolume.isBlank() || state.addSum.isBlank()) {
            viewModelScope.launch { _events.send(HistoryScreenEvent.ShowSnackbar("Заполните все поля")) }
            return
        }
        if (!isDateValid(state.addDate)) {
            viewModelScope.launch { _events.send(HistoryScreenEvent.ShowSnackbar("Неверный формат даты")) }
            return
        }

        // В дизайне модалки нет выбора автомобиля — привязываем заправку
        // к уже выбранному в фильтре авто, либо к первому доступному.
        // TODO: если у пользователя несколько машин и ни одна не выбрана в
        // фильтре — стоит либо добавить явный выбор прямо в модалку, либо
        // требовать выбрать авто в фильтре "Автомобиль" перед добавлением.
        val vehicleId = state.selectedVehicleId ?: state.userVehicles.firstOrNull()?.vehicleId
        if (vehicleId == null) {
            viewModelScope.launch {
                _events.send(HistoryScreenEvent.ShowSnackbar("Сначала добавьте автомобиль на главном экране"))
            }
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmittingRefuel = true)

            val isoDate = try {
                val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                LocalDate.parse(state.addDate, formatter).atStartOfDay().toString()
            } catch (e: Exception) {
                _events.send(HistoryScreenEvent.ShowSnackbar("Неверный формат даты"))
                _uiState.value = _uiState.value.copy(isSubmittingRefuel = false)
                return@launch
            }

            addRefuelingUseCase(
                vehicleId = vehicleId,
                gasStationPricesId = MOCK_GAS_STATION_PRICES_ID,
                volume = state.addVolume.toDoubleOrNull() ?: 0.0,
                totalSum = state.addSum.toDoubleOrNull() ?: 0.0,
                refuelDate = isoDate,
                fuelCardId = null
            ).fold(
                onSuccess = { message ->
                    _events.send(HistoryScreenEvent.ShowSnackbar(message))
                    _uiState.value = _uiState.value.copy(
                        showAddRefuelSheet = false,
                        addDate = "",
                        addVolume = "",
                        addSum = "",
                        isSubmittingRefuel = false
                    )
                    loadHistory()
                },
                onFailure = { error ->
                    _events.send(HistoryScreenEvent.ShowSnackbar("Ошибка: ${error.message}"))
                    _uiState.value = _uiState.value.copy(isSubmittingRefuel = false)
                }
            )
        }
    }
}
package com.methane.eco.trans.presentation.historyscreen

import com.methane.eco.trans.data.dto.RefuelingDto
import com.methane.eco.trans.data.dto.VehicleDto

enum class SortBy { DATE, SUM, VOLUME }

data class HistoryScreenUIState(
    val userVehicles: List<VehicleDto> = emptyList(),
    val history: List<RefuelingDto> = emptyList(),

    // --- фильтры ---
    val selectedVehicleId: String? = null,
    val sortBy: SortBy = SortBy.DATE,
    val onlyFuelCard: Boolean = false,
    val periodFilter: PeriodFilter = PeriodFilter.ALL,

    // TODO: когда появится несколько станций — заменить на реальный список АЗС с бэкенда.
    // Пока на проекте одна АГНКС, фильтр показывается декоративно.
    val selectedGasStationLabel: String = "Все АЗС",
    val currentVehicleId: String = "",

    // --- сводка "Расходы в <месяц>" + "Экономия" + "Визиты" над фильтрами ---
    val currentMonthLabel: String = "",
    val currentMonthTotalSum: Double = 0.0,
    val currentMonthTotalVolume: Double = 0.0,
    val currentMonthVisits: Int = 0,

    // TODO: расчёта экономии (сравнение со стоимостью аналога на бензине) на
    val currentMonthSavings: Double = 0.0,

    // --- модалка "Добавить заправку" (нижний bottom sheet) ---
    val showAddRefuelSheet: Boolean = false,
    val addDate: String = "",
    val addVolume: String = "",
    val addSum: String = "",
    val isSubmittingRefuel: Boolean = false,
    val isLoading: Boolean = false,
)
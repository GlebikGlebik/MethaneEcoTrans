package com.methane.eco.trans.presentation.historyscreen

import com.methane.eco.trans.data.dto.RefuelingDto
import com.methane.eco.trans.data.dto.VehicleDto

enum class SortBy { DATE, SUM, VOLUME }

data class HistoryScreenUIState(
    val userVehicles: List<VehicleDto> = emptyList(),
    val currentVehicleId: String = "",
    val history: List<RefuelingDto> = emptyList(),
    val isLoading: Boolean = false,
    // Поля для фильтров
    val selectedVehicleId: String? = null, // null означает "Все автомобили"
    val sortBy: SortBy = SortBy.DATE,
    val onlyFuelCard: Boolean = false
)
package com.methane.eco.trans.presentation.mainscreen

import com.methane.eco.trans.data.dto.VehicleDto

data class MainScreenUIState(
    val date: String = "",
    val volume: String = "",
    val sum: String = "",
    val userVehicles: List<VehicleDto> = emptyList(),
    val newVehicle: String = "",
    val currentVehicle: String = "",
    val showRefuelDialog: Boolean = false,
    val isLoading: Boolean = false
)
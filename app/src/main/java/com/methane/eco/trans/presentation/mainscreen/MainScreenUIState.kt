package com.methane.eco.trans.presentation.mainscreen

import com.methane.eco.trans.data.dto.VehicleDto

data class MainScreenUIState(
    val date: String = "",
    val volume: String = "",
    val sum: String = "",
    val fuelCardNumber: String = "",
    val userVehicles: List<VehicleDto> = emptyList(),
    val newVehiclePlate: String = "",
    val currentVehicleId: String = "",
    val showRefuelDialog: Boolean = false,
    val isLoading: Boolean = false
)
package com.methane.eco.trans.presentation.mainscreen

data class MainScreenUIState(
    val date: String = "",
    val volume: String = "",
    val sum: String = "",
    val userVehicles: List<String> = emptyList(),
    val newVehicle: String = "",
    val currentVehicle: String = "",
    val showRefuelDialog: Boolean = false,
    val isLoading: Boolean = false
)
package com.methane.eco.trans.presentation.profilescreen

import com.methane.eco.trans.data.dto.RefuelingDto
import com.methane.eco.trans.data.dto.RefuelingHistoryResponse
import com.methane.eco.trans.data.dto.VehicleDto

data class ProfileScreenUIState(
    var userName: String = "",
    var userSurname: String = "",
    var userHistory: List<RefuelingDto> = emptyList(),
    var currentVehicle: String = "",
    var userVehicles: List<VehicleDto> = emptyList(),
    var totalFuel: Double = 0.0,
    var totalSum: Double = 0.0,
    var currentMonthStats: Pair<Double, Double> = Pair(0.0, 0.0),
    var previousMonthStats: Pair<Double, Double> = Pair(0.0, 0.0),
    var monthlyComparison: Pair<Double, Double> = Pair(0.0, 0.0),
    val latestRefills: List<RefuelingDto> = emptyList(),
    val isLoading: Boolean = false
)
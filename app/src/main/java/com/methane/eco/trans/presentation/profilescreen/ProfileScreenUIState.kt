package com.methane.eco.trans.presentation.profilescreen

data class ProfileScreenUIState(
    var userName: String = "",
    var userSurname: String = "",
    var userHistory: Map<String, String> = emptyMap(),
    var currentVehicle: String = "",
    var userVehicles: List<String> = emptyList(),
    var totalFuel: Double = 0.0,
    var totalSum: Double = 0.0,
    var currentMonthStats: Pair<Double, Double> = Pair(0.0, 0.0),
    var previousMonthStats: Pair<Double, Double> = Pair(0.0, 0.0),
    var monthlyComparison: Pair<Double, Double> = Pair(0.0, 0.0)
)
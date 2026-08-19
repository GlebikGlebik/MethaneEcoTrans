package com.methane.eco.trans.domain.model

data class VehicleStats (
    val totalFuel: Double = 0.0,
    val totalSum: Double = 0.0,
    val currentMonthStats: Pair<Double, Double> = Pair(0.0, 0.0),
    val previousMonthStats: Pair<Double, Double> = Pair(0.0, 0.0),
    val monthlyComparison: Pair<Double, Double> = Pair(0.0, 0.0),
    val latestRefills: List<RefuelingRecord> = emptyList()
)
package com.methane.eco.trans.data.dto

import kotlinx.serialization.Serializable

// DTO для списка транспортных средств
@Serializable
data class VehiclesResponse(
    val vehicles: List<VehicleDto>
)

@Serializable
data class VehicleDto(
    val vehicleId: String,
    val name: String,
    val licensePlate: String?,
    val vinNumber: String,
    val model: String,
    val year: Int?,
    val mileage: Double?,
    val createdAt: String
)

// DTO для добавления ТС
@Serializable
data class CreateVehicleRequest(
    val name: String,
    val licensePlate: String?,
    val vinNumber: String,
    val model: String,
    val year: Int?,
    val mileage: Double? = 0.0
)

// DTO для заправки
@Serializable
data class CreateRefuelingRequest(
    val vehicleId: String,
    val gasStationPricesId: String,
    val volume: Double,
    val totalSum: Double,
    val refuelDate: String,
    val fuelCardId: String? = null
)

// RefuelingDto - используется в HistoryScreen
@Serializable
data class RefuelingDto(
    val refuelingId: String,
    val vehicleId: String,
    val vehicleName: String,
    val vehicleLicensePlate: String?,
    val gasStationId: String,
    val gasStationAddress: String,
    val fuelTypeId: String,
    val fuelTypeName: String,
    val volume: Double,
    val pricePerLiter: Double,
    val totalSum: Double,
    val refuelDate: String,
    val fuelCardId: String?
)

// DTO для истории заправок
@Serializable
data class RefuelingHistoryResponse(
    val refuelings: List<RefuelingDto>,
    val totalCount: Int
)
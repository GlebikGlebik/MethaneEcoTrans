package com.met.server.data.dto

import kotlinx.serialization.Serializable

// DTO для профиля пользователя
@Serializable
data class UserProfileResponse(
    val id: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val phone: String?
)

@Serializable
data class VehicleResponse(
    val vehicleId: String,
    val name: String,
    val licensePlate: String?,
    val vinNumber: String,
    val model: String,
    val year: Int?,
    val mileage: Double?,
    val createdAt: String
)

// DTO для заправки
@Serializable
data class RefuelingRequest(
    val vehicleId: String,
    val volume: Double,
    val totalSum: Double,
    val refuelDate: String
)

@Serializable
data class RefuelingResponse(
    val refuelingId: String,
    val vehicleId: String,
    val volume: Double,
    val totalSum: Double,
    val refuelDate: String
)

@Serializable
data class RefuelingHistoryResponse(
    val refuelings: List<RefuelingResponse>
)
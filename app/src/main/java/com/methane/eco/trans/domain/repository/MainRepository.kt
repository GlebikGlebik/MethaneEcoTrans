package com.methane.eco.trans.data.repository

import com.methane.eco.trans.data.dto.CreateRefuelingRequest
import com.methane.eco.trans.data.dto.CreateVehicleRequest
import com.methane.eco.trans.data.dto.RefuelingDto
import com.methane.eco.trans.data.dto.VehicleDto

interface MainRepository {
    suspend fun getVehicles(): Result<List<VehicleDto>>
    suspend fun addVehicle(request: CreateVehicleRequest): Result<String>
    suspend fun deleteVehicle(vehicleId: String): Result<Unit>
    suspend fun addRefueling(request: CreateRefuelingRequest): Result<String>
    suspend fun getRefuelingHistory(vehicleId: String? = null): Result<List<RefuelingDto>>
}
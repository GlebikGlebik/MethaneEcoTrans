package com.methane.eco.trans.domain.usecase

import com.methane.eco.trans.data.dto.*
import com.methane.eco.trans.data.repository.MainRepository

class GetVehiclesUseCase(private val _repository: MainRepository) {
    suspend operator fun invoke(): Result<List<VehicleDto>> {
        return _repository.getVehicles()
    }
}

class AddVehicleUseCase(private val _repository: MainRepository) {
    suspend operator fun invoke(
        name: String,
        licensePlate: String?,
        vinNumber: String,
        model: String,
        year: Int?,
        mileage: Double?
    ): Result<String> {
        if (name.isBlank()) return Result.failure(Exception("Название не может быть пустым"))
        if (vinNumber.length != 17) return Result.failure(Exception("VIN должен содержать 17 символов"))

        val request = CreateVehicleRequest(name, licensePlate, vinNumber, model, year, mileage)
        return _repository.addVehicle(request)
    }
}

class DeleteVehicleUseCase(private val _repository: MainRepository) {
    suspend operator fun invoke(vehicleId: String): Result<Unit> {
        return _repository.deleteVehicle(vehicleId)
    }
}

class AddRefuelingUseCase(private val _repository: MainRepository) {
    suspend operator fun invoke(
        vehicleId: String,
        gasStationId: String,
        fuelTypeId: String,
        volume: Double,
        totalSum: Double,
        refuelDate: String,
        fuelCardId: String? = null
    ): Result<String> {
        if (vehicleId.isBlank()) return Result.failure(Exception("Выберите транспортное средство"))
        if (volume <= 0) return Result.failure(Exception("Объем должен быть больше 0"))
        if (totalSum < 0) return Result.failure(Exception("Сумма не может быть отрицательной"))

        val request = CreateRefuelingRequest(vehicleId, gasStationId, fuelTypeId, volume, totalSum, refuelDate, fuelCardId)
        return _repository.addRefueling(request)
    }
}


class GetRefuelingHistoryUseCase(private val _repository: MainRepository) {
    suspend operator fun invoke(
        vehicleId: String? = null,
        startDate: String? = null,
        endDate: String? = null,
        page: Int = 0,
        size: Int = 20
    ): Result<RefuelingHistoryResponse> {
        return _repository.getRefuelingHistory(vehicleId, startDate, endDate, page, size)
    }
}
package com.methane.eco.trans.domain.usecase

import com.methane.eco.trans.data.dto.CreateRefuelingRequest
import com.methane.eco.trans.data.dto.CreateVehicleRequest
import com.methane.eco.trans.data.dto.RefuelingDto
import com.methane.eco.trans.data.dto.VehicleDto
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
        // Валидация
        if (name.isBlank()) {
            return Result.failure(Exception("Название не может быть пустым"))
        }
        if (vinNumber.length != 17) {
            return Result.failure(Exception("VIN должен содержать 17 символов"))
        }

        val request = CreateVehicleRequest(
            name = name,
            licensePlate = licensePlate,
            vinNumber = vinNumber,
            model = model,
            year = year,
            mileage = mileage
        )

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
        volume: Double,
        totalSum: Double,
        refuelDate: String
    ): Result<String> {
        // Валидация
        if (vehicleId.isBlank()) {
            return Result.failure(Exception("Выберите транспортное средство"))
        }
        if (volume <= 0) {
            return Result.failure(Exception("Объем должен быть больше 0"))
        }
        if (totalSum < 0) {
            return Result.failure(Exception("Сумма не может быть отрицательной"))
        }

        val request = CreateRefuelingRequest(
            vehicleId = vehicleId,
            volume = volume,
            totalSum = totalSum,
            refuelDate = refuelDate
        )

        return _repository.addRefueling(request)
    }
}

class GetRefuelingHistoryUseCase(private val _repository: MainRepository) {
    suspend operator fun invoke(vehicleId: String? = null): Result<List<RefuelingDto>> {
        return _repository.getRefuelingHistory(vehicleId)
    }
}
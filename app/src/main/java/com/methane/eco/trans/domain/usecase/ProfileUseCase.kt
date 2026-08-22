package com.methane.eco.trans.domain.usecase

import com.met.server.data.dto.UserProfileResponse
import com.met.server.data.dto.VehicleResponse
import com.methane.eco.trans.data.dto.RefuelingHistoryResponse
import com.methane.eco.trans.data.dto.VehicleDto
import com.methane.eco.trans.data.repository.MainRepository
import com.methane.eco.trans.domain.model.RefuelingRecord
import com.methane.eco.trans.domain.model.UserProfile
import com.methane.eco.trans.domain.model.VehicleStats
import com.methane.eco.trans.domain.repository.ProfileRepository
import java.time.LocalDate

class ProfileUseCase(private val profileRepository: ProfileRepository, val mainRepository: MainRepository) {
    suspend fun getUserProfile(): Result<UserProfileResponse> {
        return profileRepository.getProfile()
    }

    suspend fun getUserVehicles(): Result<List<VehicleDto>> {
        return mainRepository.getVehicles()
    }

    suspend fun getUserRefuelingHistory(): Result<RefuelingHistoryResponse>{
        return mainRepository.getRefuelingHistory()
    }
}
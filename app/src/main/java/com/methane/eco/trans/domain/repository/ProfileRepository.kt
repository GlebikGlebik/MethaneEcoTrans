package com.methane.eco.trans.domain.repository

import com.met.server.data.dto.UserProfileResponse
import com.methane.eco.trans.domain.model.RefuelingRecord
import com.methane.eco.trans.domain.model.UserProfile

interface ProfileRepository {
    suspend fun getProfile(): Result <UserProfileResponse>
}
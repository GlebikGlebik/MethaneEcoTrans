package com.methane.eco.trans.domain.repository

import com.met.server.data.dto.UserProfileResponse

interface ProfileRepository {
    suspend fun getProfile(): Result <UserProfileResponse>
}
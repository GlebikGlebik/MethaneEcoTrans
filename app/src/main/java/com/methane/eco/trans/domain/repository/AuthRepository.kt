package com.methane.eco.trans.domain.repository

import com.methane.eco.trans.domain.model.AuthResult

interface AuthRepository {
    suspend fun signIn(
        email: String,
        password: String,
        companyInn: String? = null
    ): AuthResult
    suspend fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phone: String?,
        userType: String = "B2C", //
        companyInn: String? = null //
    ): AuthResult
}
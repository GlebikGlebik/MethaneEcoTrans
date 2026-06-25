package com.methane.eco.trans.domain.repository

import com.methane.eco.trans.domain.model.AuthResult

// Контракт
interface AuthRepository {
    suspend fun signIn(email: String, password: String): AuthResult
    suspend fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phone: String?
    ): AuthResult
}
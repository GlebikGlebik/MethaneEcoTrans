package com.methane.eco.trans.domain.usecase

import com.methane.eco.trans.domain.repository.AuthRepository
import com.methane.eco.trans.domain.model.AuthResult

class AuthUseCase(private val _authRepository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String, companyInn: String? = null): AuthResult {
        return _authRepository.signIn(email, password, companyInn)
    }

    suspend fun regUseCase(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phone: String?,
        userType: String = "B2C",       // ✅ НОВОЕ
        companyInn: String? = null      // ✅ НОВОЕ
    ): AuthResult {
        return _authRepository.register(
            email, password, firstName, lastName, phone, userType, companyInn
        )
    }
}
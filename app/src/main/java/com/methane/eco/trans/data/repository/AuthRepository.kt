package com.methane.eco.trans.data.repository

import com.methane.eco.trans.domain.model.AuthResult
import kotlinx.coroutines.delay

// Контракт
interface AuthRepository {
    suspend fun signIn(email: String, password: String): AuthResult
}

// Mock
class MockAuthRepository : AuthRepository {
    override suspend fun signIn(email: String, password: String): AuthResult {
        // Имитируем задержку сети
        delay(1500)

        return if (email == "test@test.com" && password == "123456") {
            AuthResult.Success
        } else {
            AuthResult.Error("Неверный email или пароль.")
        }
    }
}
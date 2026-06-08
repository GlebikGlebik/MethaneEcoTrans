package com.methane.eco.trans.data.repository

import com.methane.eco.trans.domain.model.EnterResult
import kotlinx.coroutines.delay

// Контракт
interface AuthRepository {
    suspend fun signIn(email: String, password: String): EnterResult
}

// Mock
class MockAuthRepository : AuthRepository {
    override suspend fun signIn(email: String, password: String): EnterResult {
        // Имитируем задержку сети
        delay(1500)

        return if (email == "test@test.com" && password == "123456") {
            EnterResult.Success
        } else {
            EnterResult.Error("Неверный email или пароль. (Для теста: test@test.com / 123456)")
        }
    }
}
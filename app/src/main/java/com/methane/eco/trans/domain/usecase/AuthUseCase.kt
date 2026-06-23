package com.methane.eco.trans.domain.usecase

import android.util.Patterns
import com.methane.eco.trans.data.repository.AuthRepository
import com.methane.eco.trans.domain.model.AuthResult

class AuthUseCase (private val _authRepository: AuthRepository) {
    // Оператор invoke позволяет вызывать экземпляр класса как функцию: loginUseCase(email, password)
    suspend operator fun invoke(email: String, password: String): AuthResult {
        //делегируем работу с данными Репозиторию
        return _authRepository.signIn(email, password)
    }
}
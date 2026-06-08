package com.methane.eco.trans.domain.usecase

import android.util.Patterns
import com.methane.eco.trans.data.repository.AuthRepository
import com.methane.eco.trans.domain.model.AuthResult

class AuthUseCase (private val _authRepository: AuthRepository) {
    // Оператор invoke позволяет вызывать экземпляр класса как функцию: loginUseCase(email, password)
    suspend operator fun invoke(email: String, password: String): AuthResult {

        // 1. БИЗНЕС-ПРАВИЛО: Валидация пустых полей
        if (email.isBlank() || password.isBlank()) {
            return AuthResult.Error("Необходимо заполнить все поля")
        }

        // 2. БИЗНЕС-ПРАВИЛО: Валидация формата email
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return AuthResult.Error("Неверный формат email")
        }

        // 3. Если всё ок, делегируем работу с данными Репозиторию
        return _authRepository.signIn(email, password)
    }
}
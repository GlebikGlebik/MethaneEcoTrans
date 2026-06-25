package com.methane.eco.trans.domain.usecase

import android.util.Patterns
import com.methane.eco.trans.domain.model.AuthResult

class ValidationUseCase {
    private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    fun validateLoginData(
        email: String,
        password: String
    ): AuthResult {
        // 1. Убираем пробелы в начале/конце
        val trimmedEmail = email.trim()

        // 2. БИЗНЕС-ПРАВИЛО: Валидация пустых полей
        if (trimmedEmail.isBlank() || password.isBlank()) {
            return AuthResult.Error("Необходимо заполнить все поля")
        }

        // 3. БИЗНЕС-ПРАВИЛО: Валидация формата email (собственная регулярка)
        if (!trimmedEmail.matches(emailRegex)) {
            return AuthResult.Error("Неверный формат email")
        }
        return AuthResult.Success
    }

    fun validateRegisterData(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phone: String? = null
    ): AuthResult {
        val trimmedEmail = email.trim()

        if (trimmedEmail.isBlank() || password.isBlank() || firstName.isBlank() || lastName.isBlank()) {
            return AuthResult.Error("Необходимо заполнить все обязательные поля")
        }

        if (!trimmedEmail.matches(emailRegex)) {
            return AuthResult.Error("Неверный формат email")
        }

        if (password.length < 8) {
            return AuthResult.Error("Пароль должен содержать минимум 8 символов")
        }

        // 4. Валидация телефона (если указан)
        if (!phone.isNullOrBlank()) {
            // Простая проверка формата +7XXXXXXXXXX
            val phoneRegex = Regex("^\\+7\\d{10}$")
            if (!phone.matches(phoneRegex)) {
                return AuthResult.Error("Неверный формат телефона. Используйте формат: +7XXXXXXXXXX")
            }
        }
        return AuthResult.Success
    }
}
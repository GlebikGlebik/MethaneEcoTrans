package com.methane.eco.trans.domain.usecase

import com.methane.eco.trans.domain.model.AuthResult

class ValidationUseCase {
    private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun validateLoginData(email: String, password: String): AuthResult {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank() || password.isBlank()) {
            return AuthResult.Error("Необходимо заполнить все поля")
        }
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
        phone: String? = null,
        userType: String = "B2C",
        companyInn: String? = null
    ): AuthResult {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank() || password.isBlank() ||
            firstName.isBlank() || lastName.isBlank()) {
            return AuthResult.Error("Необходимо заполнить все обязательные поля")
        }
        if (!trimmedEmail.matches(emailRegex)) {
            return AuthResult.Error("Неверный формат email")
        }
        if (password.length < 8) {
            return AuthResult.Error("Пароль должен содержать минимум 8 символов")
        }
        if (!phone.isNullOrBlank()) {
            val phoneRegex = Regex("^\\+7\\d{10}$")
            if (!phone.matches(phoneRegex)) {
                return AuthResult.Error("Неверный формат телефона. Используйте формат: +7XXXXXXXXXX")
            }
        }

        // Валидация типа пользователя
        if (userType !in listOf("B2C", "B2B")) {
            return AuthResult.Error("Неверный тип пользователя")
        }

        // B2B пользователь обязан указать ИНН
        if (userType == "B2B" && companyInn.isNullOrBlank()) {
            return AuthResult.Error("Для корпоративного аккаунта необходимо указать ИНН компании")
        }

        return AuthResult.Success
    }
}
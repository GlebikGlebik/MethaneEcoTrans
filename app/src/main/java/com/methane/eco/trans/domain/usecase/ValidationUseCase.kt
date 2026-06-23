package com.methane.eco.trans.domain.usecase

import android.util.Patterns
import com.methane.eco.trans.domain.model.AuthResult

class ValidationUseCase {
    fun validateLoginData(
        email: String,
        password: String
    ): AuthResult {
        if (email.isBlank() || password.isBlank()) {
            return AuthResult.Error("Необходимо заполнить все поля")
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return AuthResult.Error("Неверный формат email")
        }
        return AuthResult.Success
    }
}
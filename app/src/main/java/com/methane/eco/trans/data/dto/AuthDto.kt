package com.methane.eco.trans.data.dto

import kotlinx.serialization.Serializable

// Запрос регистрации (должен соответствовать серверному RegisterRequest)
@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val phone: String? = null
)

// Запрос входа (должен соответствовать серверному LoginRequest)
@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

// Ответ сервера с токеном (должен соответствовать серверному AuthResponse)
@Serializable
data class AuthResponse(
    val token: String,
    val userId: String,
    val email: String,
    val firstName: String,
    val lastName: String
)

// Ответ с ошибкой (должен соответствовать серверному ErrorResponse)
@Serializable
data class ErrorResponse(
    val message: String
)
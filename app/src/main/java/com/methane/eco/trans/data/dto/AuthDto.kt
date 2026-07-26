package com.methane.eco.trans.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val phone: String? = null,
    val userType: String = "B2C",
    val companyInn: String? = null
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val token: String,
    val userId: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val userType: String,
    val companyInn: String? = null
)

@Serializable
data class ErrorResponse(
    val message: String
)
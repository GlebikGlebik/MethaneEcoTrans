package com.methane.eco.trans.presentation.enterscreen

data class EnterScreenUiState(
    val email: String = "",
    val password: String = "",
    val companyInn: String = "", // Для B2B
    val isB2B: Boolean = false, // Флаг типа пользователя
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false
)
package com.MethaneEcoTrans.METR

data class AuthUiState(
    var password: String = "",
    var email: String = "",
    var name: String = "",
    var surname: String = "",
    var isLoading: Boolean = false,
    var error: String? = null,
    val isRegistrationSuccessful: Boolean = false,
)
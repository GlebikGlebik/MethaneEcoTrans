package com.methane.eco.trans.presentation.regscreen

data class RegScreenUIState(
    val password: String = "",
    val email: String = "",
    val name: String = "",
    val surname: String = "",
    val userType: String = "B2C",           // String вместо String?
    val companyInn: String = "",            // String вместо String?
    val phone: String? = null,
    val isLoading: Boolean = false
) {
    // Удобное свойство для проверки в UI
    val isB2B: Boolean get() = userType == "B2B"
}
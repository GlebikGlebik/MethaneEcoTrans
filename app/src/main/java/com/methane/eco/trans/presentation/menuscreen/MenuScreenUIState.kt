package com.methane.eco.trans.presentation.menuscreen

data class MenuScreenUIState(
    // --- окно "Промокод" (bottom sheet) ---
    val showPromoSheet: Boolean = false,
    val promoCode: String = "",
    // Ошибка показывается прямо в окне, под полем ввода, а не снэкбаром:
    val promoError: String? = null,
    val isApplyingPromo: Boolean = false,
    // --- онтакты и поддержка
    val supportPhone: String = "8 912 102 44 10"
)
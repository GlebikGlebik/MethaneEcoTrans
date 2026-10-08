package com.methane.eco.trans.presentation.menuscreen

sealed class MenuScreenEvent {
    data class ShowSnackbar(val message: String) : MenuScreenEvent()

    // Пункты меню
    object NavigateToSettingsScreen : MenuScreenEvent()
    object NavigateToHistoryScreen : MenuScreenEvent()

    // Нижняя навигация
    object NavigateToHomeScreen : MenuScreenEvent()
    object NavigateToQrScreen : MenuScreenEvent()
    object NavigateToStatsScreen : MenuScreenEvent()
}
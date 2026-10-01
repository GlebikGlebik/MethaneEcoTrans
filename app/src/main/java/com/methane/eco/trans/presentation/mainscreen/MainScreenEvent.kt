package com.methane.eco.trans.presentation.mainscreen

sealed class MainScreenEvent {
    data class ShowSnackbar(val message: String): MainScreenEvent()
    object NavigateToHistoryScreen: MainScreenEvent()
    object NavigateToProfileScreen: MainScreenEvent()
    object NavigateToSettingsScreen: MainScreenEvent()
    object NavigateToQrScreen: MainScreenEvent()
    object NavigateToMoreScreen: MainScreenEvent()
    object NavigateToHomeScreen: MainScreenEvent()
}
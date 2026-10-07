package com.methane.eco.trans.presentation.homescreen

sealed class HomeScreenEvent {
    data class ShowSnackbar(val message: String): HomeScreenEvent()
    object NavigateToHistoryScreen: HomeScreenEvent()
    object NavigateToProfileScreen: HomeScreenEvent()
    object NavigateToSettingsScreen: HomeScreenEvent()
    object NavigateToQrScreen: HomeScreenEvent()
    object NavigateToMoreScreen: HomeScreenEvent()
    object NavigateToHomeScreen: HomeScreenEvent()
}
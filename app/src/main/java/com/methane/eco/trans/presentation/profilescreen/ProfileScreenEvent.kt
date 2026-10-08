package com.methane.eco.trans.presentation.profilescreen

sealed class ProfileScreenEvent {
    data class ShowSnackbar(val message: String): ProfileScreenEvent()
    object NavigateToHistoryScreen: ProfileScreenEvent()
    object NavigateToHomeScreen: ProfileScreenEvent()
}
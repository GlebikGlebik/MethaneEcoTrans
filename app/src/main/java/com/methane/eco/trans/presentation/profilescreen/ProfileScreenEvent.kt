package com.methane.eco.trans.presentation.profilescreen

import com.methane.eco.trans.presentation.mainscreen.MainScreenEvent

sealed class ProfileScreenEvent {
    data class ShowSnackbar(val message: String): ProfileScreenEvent()
    object NavigateToHistoryScreen: ProfileScreenEvent()
    object NavigateToMainScreen: ProfileScreenEvent()
}
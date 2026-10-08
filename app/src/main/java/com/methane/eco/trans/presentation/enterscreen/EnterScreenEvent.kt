package com.methane.eco.trans.presentation.enterscreen

sealed class EnterScreenEvent {
    data class ShowSnackbar(val message: String) : EnterScreenEvent()
    object NavigateToHomeScreen : EnterScreenEvent()
    object NavigateToRegistrationScreen : EnterScreenEvent()
}
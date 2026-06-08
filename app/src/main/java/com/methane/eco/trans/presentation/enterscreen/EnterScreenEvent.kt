package com.methane.eco.trans.presentation.enterscreen

sealed class EnterScreenEvent {
    data class ShowSnackbar(val message: String) : EnterScreenEvent()
    object NavigateToMainScreen : EnterScreenEvent()
    object NavigateToRegistrationScreen : EnterScreenEvent()
}
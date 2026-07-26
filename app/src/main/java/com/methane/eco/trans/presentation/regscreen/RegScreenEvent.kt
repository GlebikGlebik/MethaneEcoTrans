package com.methane.eco.trans.presentation.regscreen

sealed class RegScreenEvent {
    data class ShowSnackbar(val message: String) : RegScreenEvent()
    object NavigateToMainScreen : RegScreenEvent()
    object NavigateToEnterScreen : RegScreenEvent()
}
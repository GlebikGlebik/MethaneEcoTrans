package com.methane.eco.trans.presentation.regscreen

sealed class RegScreenEvent {
    data class ShowSnackbar(val message: String) : RegScreenEvent()
    object NavigateToHomeScreen : RegScreenEvent()
    object NavigateToEnterScreen : RegScreenEvent()
}
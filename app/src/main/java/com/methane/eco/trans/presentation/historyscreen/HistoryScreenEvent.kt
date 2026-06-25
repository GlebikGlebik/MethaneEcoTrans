package com.methane.eco.trans.presentation.historyscreen

sealed class HistoryScreenEvent {
    data class ShowSnackbar(val message: String) : HistoryScreenEvent()
    object NavigateToMainScreen : HistoryScreenEvent()
    object NavigateToProfileScreen : HistoryScreenEvent()
}
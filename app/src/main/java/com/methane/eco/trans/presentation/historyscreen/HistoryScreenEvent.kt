package com.methane.eco.trans.presentation.historyscreen

sealed class HistoryScreenEvent {
    data class ShowSnackbar(val message: String) : HistoryScreenEvent()
    object NavigateToHomeScreen : HistoryScreenEvent()
    object NavigateToProfileScreen : HistoryScreenEvent()
    object NavigateToQrScreen : HistoryScreenEvent()
    object NavigateToMenuScreen : HistoryScreenEvent()
}
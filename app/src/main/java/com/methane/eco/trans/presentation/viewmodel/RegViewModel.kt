package com.methane.eco.trans.presentation.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import com.methane.eco.trans.presentation.regscreen.RegScreenEvent
import com.methane.eco.trans.presentation.regscreen.RegScreenUIState

class RegViewModel: ViewModel(

) {
    private val _uiState = MutableStateFlow(RegScreenUIState())
    val uiState: StateFlow<RegScreenUIState> = _uiState.asStateFlow()

    private val _event = Channel<RegScreenEvent>()
    val event = _event.receiveAsFlow()

    fun onEmailChanged(newEmail: String) {
        _uiState.value = _uiState.value.copy(
            email = newEmail
        )
    }

    fun onPasswordChanged(newPassword: String) {
        _uiState.value = _uiState.value.copy(
            password = newPassword
        )
    }
}
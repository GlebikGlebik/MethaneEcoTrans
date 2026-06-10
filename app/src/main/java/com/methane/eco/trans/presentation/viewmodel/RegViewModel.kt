package com.methane.eco.trans.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.methane.eco.trans.domain.usecase.AuthUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import com.methane.eco.trans.presentation.regscreen.RegScreenEvent
import com.methane.eco.trans.presentation.regscreen.RegScreenUIState
import kotlinx.coroutines.launch

class RegViewModel(
    private val authUseCase: AuthUseCase
): ViewModel() {
    private val _uiState = MutableStateFlow(RegScreenUIState())
    val uiState: StateFlow<RegScreenUIState> = _uiState.asStateFlow()

    private val _events = Channel<RegScreenEvent>()
    val events = _events.receiveAsFlow()

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

    fun onNameChanged(newName: String) {
        _uiState.value = _uiState.value.copy(
            name = newName
        )
    }

    fun onSurnameChanged(newSurname: String) {
        _uiState.value = _uiState.value.copy(
            surname = newSurname
        )
    }

    fun onIsLoadingChanged(NewIsLoading: Boolean) {
        _uiState.value = _uiState.value.copy(
            isLoading = NewIsLoading
        )
    }

    // Обработчик нажатия на кнопку регистрации
    fun onRegisterClick(): Unit {
        return
    }

    fun onEnterClicked() {
        viewModelScope.launch {
            _events.send(RegScreenEvent.NavigateToEnterScreen)
        }
    }
}
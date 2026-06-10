package com.methane.eco.trans.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.methane.eco.trans.domain.model.AuthResult
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import com.methane.eco.trans.domain.usecase.AuthUseCase
import com.methane.eco.trans.presentation.enterscreen.EnterScreenUiState
import com.methane.eco.trans.presentation.enterscreen.EnterScreenEvent

class EnterViewModel(
    private val authUseCase: AuthUseCase
): ViewModel() {

    private val _uiState = MutableStateFlow(EnterScreenUiState())
    val uiState: StateFlow<EnterScreenUiState> = _uiState.asStateFlow()
    private val _events = Channel<EnterScreenEvent>()
    val events = _events.receiveAsFlow()

    fun onEmailChanged(newEmail: String) {
        _uiState.value = _uiState.value.copy(
            email = newEmail,
            emailError = null
        )
    }

    fun onPasswordChanged(newPassword: String) {
        _uiState.value = _uiState.value.copy(
            password = newPassword,
            passwordError = null
        )
    }

    fun onIsLoadingChanged(newIsLoading: Boolean) {
        _uiState.value = _uiState.value.copy(
            isLoading = newIsLoading
        )
    }


    fun onEnterClicked() {
        val currentState = _uiState.value

        viewModelScope.launch {
            // показываем загрузку
            onIsLoadingChanged(true)

            // вызываем useCase и обрабатываем результат
            when (val result = authUseCase(currentState.email, currentState.password)) {
                is AuthResult.Success -> {
                    _events.send(EnterScreenEvent.NavigateToMainScreen)
                }
                is AuthResult.Error -> {
                    _events.send(EnterScreenEvent.ShowSnackbar(result.message))
                }
            }
            onIsLoadingChanged(false)
        }
    }

    fun onRegistrationClicked() {
        // Отправляем событие навигации
        viewModelScope.launch {
            _events.send(EnterScreenEvent.NavigateToRegistrationScreen)
        }
    }
}
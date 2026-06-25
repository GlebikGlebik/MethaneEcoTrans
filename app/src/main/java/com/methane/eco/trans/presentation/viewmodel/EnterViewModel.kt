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
import com.methane.eco.trans.domain.usecase.ValidationUseCase
import com.methane.eco.trans.presentation.enterscreen.EnterScreenUiState
import com.methane.eco.trans.presentation.enterscreen.EnterScreenEvent

class EnterViewModel(
    private val authUseCase: AuthUseCase,
    private val validationUseCase: ValidationUseCase
): ViewModel() {
    private val _uiState = MutableStateFlow(EnterScreenUiState())
    val uiState: StateFlow<EnterScreenUiState> = _uiState.asStateFlow()

    private val _events = Channel<EnterScreenEvent>()
    val events = _events.receiveAsFlow()

    fun onEmailChanged(newEmail: String) {
        _uiState.value = _uiState.value.copy(email = newEmail, emailError = null)
    }

    fun onPasswordChanged(newPassword: String) {
        _uiState.value = _uiState.value.copy(password = newPassword, passwordError = null)
    }

    fun onCompanyInnChanged(newInn: String) {
        _uiState.value = _uiState.value.copy(companyInn = newInn)
    }

    fun onUserTypeChanged(newType: String) {
        _uiState.value = _uiState.value.copy(isB2B = newType == "B2B")
    }

    fun onIsLoadingChanged(newIsLoading: Boolean) {
        _uiState.value = _uiState.value.copy(isLoading = newIsLoading)
    }

    fun onEnterClicked() {
        val currentState = _uiState.value
        viewModelScope.launch {
            onIsLoadingChanged(true)
            val result: AuthResult

            val validationResult = validationUseCase.validateLoginData(
                currentState.email,
                currentState.password
            )

            result = if (validationResult == AuthResult.Success){
                authUseCase(
                    currentState.email,
                    currentState.password,
                    if (currentState.isB2B) currentState.companyInn else null
                )
            } else {
                validationResult
            }

            when (result) {
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
        viewModelScope.launch {
            _events.send(EnterScreenEvent.NavigateToRegistrationScreen)
        }
    }
}
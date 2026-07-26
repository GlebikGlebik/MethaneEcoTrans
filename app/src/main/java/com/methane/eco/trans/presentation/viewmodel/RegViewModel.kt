package com.methane.eco.trans.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.methane.eco.trans.domain.model.AuthResult
import com.methane.eco.trans.domain.usecase.AuthUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import com.methane.eco.trans.presentation.regscreen.RegScreenEvent
import com.methane.eco.trans.presentation.regscreen.RegScreenUIState
import com.methane.eco.trans.domain.usecase.ValidationUseCase
import kotlinx.coroutines.launch

class RegViewModel(
    private val authUseCase: AuthUseCase,
    private val validationUseCase: ValidationUseCase
): ViewModel() {
    private val _uiState = MutableStateFlow(RegScreenUIState())
    val uiState: StateFlow<RegScreenUIState> = _uiState.asStateFlow()

    private val _events = Channel<RegScreenEvent>()
    val events = _events.receiveAsFlow()

    fun onEmailChanged(newEmail: String) {
        _uiState.value = _uiState.value.copy(email = newEmail)
    }

    fun onPasswordChanged(newPassword: String) {
        _uiState.value = _uiState.value.copy(password = newPassword)
    }

    fun onNameChanged(newName: String) {
        _uiState.value = _uiState.value.copy(name = newName)
    }

    fun onSurnameChanged(newSurname: String) {
        _uiState.value = _uiState.value.copy(surname = newSurname)
    }

    fun onPhoneChanged(newPhone: String?) {
        _uiState.value = _uiState.value.copy(phone = newPhone)
    }

    fun onUserTypeChanged(type: String) {
        _uiState.value = _uiState.value.copy(
            userType = type,
            companyInn = if (type == "B2C") "" else _uiState.value.companyInn
        )
    }

    fun onCompanyInnChanged(inn: String) {
        _uiState.value = _uiState.value.copy(companyInn = inn)
    }

    fun onIsLoadingChanged(newIsLoading: Boolean) {
        _uiState.value = _uiState.value.copy(isLoading = newIsLoading)
    }

    fun onRegisterClick() {
        val currentState = _uiState.value
        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true)
            try {
                val validationResult = validationUseCase.validateRegisterData(
                    email = currentState.email,
                    password = currentState.password,
                    firstName = currentState.name,
                    lastName = currentState.surname,
                    phone = currentState.phone,
                    userType = currentState.userType,
                    companyInn = currentState.companyInn
                )

                val result = if (validationResult == AuthResult.Success) {
                    authUseCase.regUseCase(
                        email = currentState.email,
                        password = currentState.password,
                        firstName = currentState.name,
                        lastName = currentState.surname,
                        phone = currentState.phone,
                        userType = currentState.userType,
                        companyInn = currentState.companyInn.takeIf { it.isNotBlank() }
                    )
                } else {
                    validationResult
                }

                when (result) {
                    is AuthResult.Success -> _events.send(RegScreenEvent.NavigateToMainScreen)
                    is AuthResult.Error -> _events.send(RegScreenEvent.ShowSnackbar(result.message))
                }
            } catch (e: Exception) {
                _events.send(RegScreenEvent.ShowSnackbar("Ошибка: ${e.message}"))
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun onEnterClicked() {
        viewModelScope.launch {
            _events.send(RegScreenEvent.NavigateToEnterScreen)
        }
    }
}
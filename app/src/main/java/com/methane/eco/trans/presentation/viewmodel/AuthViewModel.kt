package com.methane.eco.trans.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import android.util.Patterns
import com.methane.eco.trans.presentation.dataclass.EnterScreenUiState
import com.methane.eco.trans.presentation.sealedclass.EnterScreenEvent

class AuthViewModel: ViewModel() {
    //private_variables
    private val _uiState = MutableStateFlow(EnterScreenUiState())
    private val _events = Channel<EnterScreenEvent>()

    //variables
    val uiState: StateFlow<EnterScreenUiState> = _uiState.asStateFlow()
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


    fun onLoginClicked() {
        val currentState = _uiState.value

        // Валидация (В будущем перенесем в UseCase)
        if (currentState.email.isEmpty() || currentState.password.isEmpty()) {
            _uiState.value = currentState.copy(passwordError = "Заполните все поля")
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(currentState.email).matches()) {
            _uiState.value = currentState.copy(emailError = "Неверный формат email")
            return
        }

        // Запускаем корутину для авторизации
        loginUser(currentState.email, currentState.password)
    }

    fun onRegistrationClicked() {
        // Отправляем событие навигации
        viewModelScope.launch {
            _events.send(EnterScreenEvent.NavigateToRegistrationScreen)
        }
    }

    // --- Приватные методы бизнес-логики (В будущем перенесем в Repository/UseCase) ---

    private fun loginUser(email: String, password: String) {
        // viewModelScope автоматически отменяет корутину, если ViewModel уничтожается
        viewModelScope.launch {
            // 1. Показываем индикатор загрузки
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // 2. Имитация запроса в Firebase (Заменим на Repository в будущем)
                // val result = authRepository.signIn(email, password)
                val isSuccessful = mockFirebaseSignIn(email, password)

                if (isSuccessful) {
                    // 3. Успех: отправляем событие навигации
                    _events.send(EnterScreenEvent.NavigateToMainScreen)
                } else {
                    // 4. Ошибка: отправляем событие снекбара
                    _events.send(EnterScreenEvent.ShowSnackbar("Неверный email или пароль"))
                }
            } catch (e: Exception) {
                _events.send(EnterScreenEvent.ShowSnackbar("Ошибка входа: ${e.message}"))
            } finally {
                // 5. В любом случае убираем индикатор загрузки
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    // ВРЕМЕННАЯ ЗАГЛУШКА (Уберем при создании Data слоя)
    private suspend fun mockFirebaseSignIn(email: String, password: String): Boolean {
        kotlinx.coroutines.delay(1500) // Имитация задержки сети
        return email == "test@test.com" && password == "123456"
    }

}
package com.methane.eco.trans.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.methane.eco.trans.presentation.menuscreen.MenuScreenEvent
import com.methane.eco.trans.presentation.menuscreen.MenuScreenUIState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class MenuScreenViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MenuScreenUIState())
    val uiState: StateFlow<MenuScreenUIState> = _uiState.asStateFlow()

    private val _events = Channel<MenuScreenEvent>()
    val events = _events.receiveAsFlow()

    private companion object {
        const val MAX_PROMO_LENGTH = 32
    }


    //TODO: промокоды у нас пока что не работают
    // ================== Промокод ==================
    fun onPromoClicked() {
        _uiState.value = _uiState.value.copy(showPromoSheet = true)
    }

    fun onPromoSheetDismissed() {
        // закрываем окно и сбрасываем черновик, чтобы в следующий раз оно открылось чистым
        _uiState.value = _uiState.value.copy(
            showPromoSheet = false,
            promoCode = "",
            promoError = null,
            isApplyingPromo = false
        )
    }

    fun onPromoCodeChanged(value: String) {
        if (value.length <= MAX_PROMO_LENGTH) {
            // при любом изменении текста убираем старую ошибку
            _uiState.value = _uiState.value.copy(promoCode = value, promoError = null)
        }
    }
    
    fun onApplyPromoClicked() {
        viewModelScope.launch{
            _events.send(MenuScreenEvent.ShowSnackbar("Промокод указан неверно"))
        }
    }

    // ================== Пункты меню ==================

    fun onSettingsClicked() {
        viewModelScope.launch { _events.send(MenuScreenEvent.NavigateToSettingsScreen) }
    }

    fun onHistoryClicked() {
        viewModelScope.launch { _events.send(MenuScreenEvent.NavigateToHistoryScreen) }
    }

    // Заглушка для пунктов, экраны которых ещё не сделаны.
    fun onComingSoonClicked(title: String) {
        viewModelScope.launch {
            _events.send(MenuScreenEvent.ShowSnackbar("«$title» — раздел скоро будет доступен"))
        }
    }

    // ================== Нижняя навигация ==================

    fun onHomeClicked() {
        viewModelScope.launch { _events.send(MenuScreenEvent.NavigateToHomeScreen) }
    }

    fun onQrClicked() {
        viewModelScope.launch { _events.send(MenuScreenEvent.NavigateToQrScreen) }
    }

    fun onStatsClicked() {
        viewModelScope.launch { _events.send(MenuScreenEvent.NavigateToStatsScreen) }
    }
}
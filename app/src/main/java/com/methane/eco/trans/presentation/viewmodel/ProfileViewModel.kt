package com.methane.eco.trans.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.methane.eco.trans.data.dto.RefuelingDto
import com.methane.eco.trans.data.dto.RefuelingHistoryResponse
import com.methane.eco.trans.data.dto.VehicleDto
import com.methane.eco.trans.domain.usecase.AddRefuelingUseCase
import com.methane.eco.trans.domain.usecase.AddVehicleUseCase
import com.methane.eco.trans.domain.usecase.DeleteVehicleUseCase
import com.methane.eco.trans.domain.usecase.GetRefuelingHistoryUseCase
import com.methane.eco.trans.domain.usecase.GetVehiclesUseCase
import com.methane.eco.trans.domain.usecase.ProfileUseCase
import com.methane.eco.trans.presentation.mainscreen.MainScreenEvent
import com.methane.eco.trans.presentation.mainscreen.MainScreenUIState
import com.methane.eco.trans.presentation.profilescreen.ProfileScreenEvent
import com.methane.eco.trans.presentation.profilescreen.ProfileScreenUIState
import io.ktor.client.call.body
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ProfileViewModel(
    val profileUseCase: ProfileUseCase
): ViewModel() {
    private val _uiState = MutableStateFlow(ProfileScreenUIState())
    val uiState: StateFlow<ProfileScreenUIState> = _uiState.asStateFlow()

    private val _events = Channel<ProfileScreenEvent>()
    val events = _events.receiveAsFlow()

    init{
        try{
            _uiState.value = _uiState.value.copy(isLoading = true)
            loadUserProfile()
            loadUserHistory()
            loadUserVehicles()
        } catch (e: Exception){
            Log.e("MainRepository", "Ошибка получения данных пользователя: ${e.message}", e)
        }


    }

    private fun loadUserProfile(){
        viewModelScope.launch {
            val profile = profileUseCase.getUserProfile()
        }
    }

    private fun loadUserHistory(){
        viewModelScope.launch {
            profileUseCase.getUserRefuelingHistory().fold(
                onSuccess = { history ->
                    _uiState.value = _uiState.value.copy(
                        userHistory = history.refuelings
                    )
                },
                onFailure = { error ->
                    _events.send(ProfileScreenEvent.ShowSnackbar("${error.message}"))
                }
            )
        }
    }

    private fun loadUserVehicles(){
        viewModelScope.launch{
            _uiState.value = _uiState.value.copy(isLoading = true)
            //получаем_список_автомобилей
            profileUseCase.getUserVehicles().fold(
                onSuccess = { vehicles ->
                    _uiState.value = _uiState.value.copy(
                        userVehicles = vehicles
                    )
                },
                onFailure = { error ->
                    _events.send(ProfileScreenEvent.ShowSnackbar("${error.message}"))
                }
            )
        }
    }

    //сеттеры
    fun onUserNameChanged(newUserName: String){
        _uiState.value = _uiState.value.copy(
            userName = newUserName
        )
    }

    fun onUserSurnameChanged(newUserSurname: String){
        _uiState.value = _uiState.value.copy(
            userSurname = newUserSurname
        )
    }

    fun onUserHistoryChanged(newUserHistory: List<RefuelingDto> ){
        _uiState.value = _uiState.value.copy(
            userHistory = newUserHistory
        )
    }

    fun onCurrentVehicleChanged(newCurrentVehicle: String){
        _uiState.value = _uiState.value.copy(
            currentVehicle = newCurrentVehicle
        )
    }

    fun onUserVehiclesChanged(newUserVehicles: List<VehicleDto>){
        _uiState.value = _uiState.value.copy(
            userVehicles = newUserVehicles
        )
    }

    fun onTotalFuelChanged(newTotalFuel: Double){
        _uiState.value = _uiState.value.copy(
            totalFuel = newTotalFuel
        )
    }

    fun onTotalSumChanged(newTotalSum: Double){
        _uiState.value = _uiState.value.copy(
            totalSum = newTotalSum
        )
    }

    fun onCMSChanged(newCMS: Pair<Double, Double>){
        _uiState.value = _uiState.value.copy(
           currentMonthStats = newCMS
        )
    }

    fun onPMSChanged(newPMS: Pair<Double, Double>){
        _uiState.value = _uiState.value.copy(
            previousMonthStats = newPMS
        )
    }

    fun onMonthlyComparison(newMonthlyComparison: Pair<Double, Double>){
        _uiState.value = _uiState.value.copy(
            monthlyComparison = newMonthlyComparison
        )
    }


}
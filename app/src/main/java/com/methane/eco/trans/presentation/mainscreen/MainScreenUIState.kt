package com.methane.eco.trans.presentation.mainscreen

import com.methane.eco.trans.data.dto.VehicleDto
import com.methane.eco.trans.domain.model.HomeActionItem
import com.methane.eco.trans.domain.model.defaultNewsItems
import com.methane.eco.trans.domain.model.defaultServiceItems

data class MainScreenUIState(
    val date: String = "",
    val userFullName: String = "",
    val volume: String = "",
    val sum: String = "",
    val discountCardID: String = "",
    val fuelCardNumber: String = "",
    val discountAmount: Int = 0,
    val userVehicles: List<VehicleDto> = emptyList(),
    val newVehiclePlate: String = "",
    val currentVehicleId: String = "",
    val newsItems: List<HomeActionItem> = defaultNewsItems(),
    val serviceItems: List<HomeActionItem> = defaultServiceItems(),
    val ecoStatsSummary: String = "Узнайте, какой вклад в защиту природы вы внесли, пользуясь ЭКО топливом",
    val showRefuelDialog: Boolean = false,
    val isLoading: Boolean = false
)
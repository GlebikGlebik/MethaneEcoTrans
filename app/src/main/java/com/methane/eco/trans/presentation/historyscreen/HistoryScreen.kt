@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@file:Suppress("UNCHECKED_CAST")

package com.methane.eco.trans.presentation.historyscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.methane.eco.trans.R
import com.methane.eco.trans.data.dto.RefuelingDto
import com.methane.eco.trans.data.local.TokenStorage
import com.methane.eco.trans.data.repository.MainRepositoryImpl
import com.methane.eco.trans.domain.usecase.AddRefuelingUseCase
import com.methane.eco.trans.domain.usecase.GetRefuelingHistoryUseCase
import com.methane.eco.trans.domain.usecase.GetVehiclesUseCase
import com.methane.eco.trans.presentation.components.AppBottomNavBar
import com.methane.eco.trans.presentation.components.NavBarItem
import com.methane.eco.trans.presentation.viewmodel.HistoryViewModel
import com.methane.eco.trans.segoe_ui
import com.methane.eco.trans.segoe_ui_bold
import com.methane.eco.trans.theme.CustomCarpiBlue
import com.methane.eco.trans.theme.CustomEnterBarColor
import com.methane.eco.trans.theme.CustomGrey
import com.methane.eco.trans.theme.CustomTrafficWhite
import com.methane.eco.trans.theme.CustomTurquoiseBlue
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val monthNames = listOf(
    "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
    "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
)

@Composable
fun HistoryScreen(
    navController: NavController,
    viewModel: HistoryViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val context = navController.context
                val tokenStorage = TokenStorage(context)
                val repository = MainRepositoryImpl(tokenStorage)

                return HistoryViewModel(
                    GetVehiclesUseCase(repository),
                    GetRefuelingHistoryUseCase(repository),
                    AddRefuelingUseCase(repository)
                ) as T
            }
        }
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is HistoryScreenEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is HistoryScreenEvent.NavigateToMainScreen -> navController.navigate("MainScreen")
                is HistoryScreenEvent.NavigateToProfileScreen -> navController.navigate("ProfileScreen")
                is HistoryScreenEvent.NavigateToQrScreen -> navController.navigate("QrScreen")
                is HistoryScreenEvent.NavigateToMoreScreen -> navController.navigate("MoreScreen")
            }
        }
    }

    val groupedHistory = remember(uiState.history) {
        uiState.history.groupBy { refueling ->
            try {
                val parts = refueling.refuelDate.substringBefore('T').split("-")
                val year = parts[0].toInt()
                val month = parts[1].toInt()
                "$month;$year"
            } catch (e: Exception) {
                "0;0"
            }
        }.toSortedMap(compareByDescending {
            val parts = it.split(";")
            parts[1].toInt() * 100 + parts[0].toInt()
        })
    }

    Scaffold(
        containerColor = CustomTrafficWhite,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            AppBottomNavBar(
                items = listOf(
                    NavBarItem(R.drawable.vector_home, "homeIcon", onClick = viewModel::onMainClicked),
                    NavBarItem(R.drawable.vector_history, "historyIcon", onClick = {}, highlighted = true),
                    NavBarItem(R.drawable.vector_qr, "qrIcon", onClick = viewModel::onQrClicked),
                    NavBarItem(R.drawable.vector_stats, "statsIcon", onClick = viewModel::onProfileClicked),
                    NavBarItem(R.drawable.vector_more, "moreIcon", onClick = viewModel::onMoreClicked)
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(CustomTrafficWhite),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding() + 12.dp
            )
        ) {
            item {
                HistoryHeader(onAddClick = { viewModel.onShowAddRefuelSheetChanged(true) })
            }

            item {
                Spacer(Modifier.height(8.dp))
                MonthSummaryRow(
                    monthLabel = uiState.currentMonthLabel,
                    totalSum = uiState.currentMonthTotalSum,
                    totalVolume = uiState.currentMonthTotalVolume,
                    savings = uiState.currentMonthSavings,
                    visits = uiState.currentMonthVisits
                )
            }

            item {
                Spacer(Modifier.height(16.dp))
                FilterChipsRow(
                    uiState = uiState,
                    onOnlyFuelCardChanged = viewModel::onOnlyFuelCardChanged,
                    onSortByChanged = viewModel::onSortByChanged,
                    onVehicleFilterChanged = viewModel::onVehicleFilterChanged,
                    onPeriodFilterChanged = viewModel::onPeriodFilterChanged
                )
                Spacer(Modifier.height(16.dp))
            }

            when {
                uiState.isLoading -> {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = CustomTurquoiseBlue)
                        }
                    }
                }

                groupedHistory.isEmpty() -> {
                    item {
                        Text(
                            text = "Нет данных о заправках",
                            color = CustomGrey,
                            fontFamily = segoe_ui,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(40.dp)
                        )
                    }
                }

                else -> {
                    groupedHistory.forEach { (monthYearKey, records) ->
                        val parts = monthYearKey.split(";")
                        val month = parts[0].toIntOrNull() ?: 0
                        val year = parts.getOrElse(1) { "" }
                        val monthLabel = "${monthNames.getOrElse(month - 1) { "Месяц" }} $year"
                        val totalSum = records.sumOf { it.totalSum }

                        item {
                            MonthCard(monthLabel = monthLabel, totalSum = totalSum, entries = records)
                            Spacer(Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }

    if (uiState.showAddRefuelSheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.onShowAddRefuelSheetChanged(false) },
            containerColor = CustomTrafficWhite
        ) {
            AddRefuelSheetContent(
                date = uiState.addDate,
                volume = uiState.addVolume,
                sum = uiState.addSum,
                isSubmitting = uiState.isSubmittingRefuel,
                onDateChanged = viewModel::onAddDateChanged,
                onVolumeChanged = viewModel::onAddVolumeChanged,
                onSumChanged = viewModel::onAddSumChanged,
                onSubmit = viewModel::onSubmitAddRefueling
            )
        }
    }
}

// ============================== Шапка ==============================

@Composable
private fun HistoryHeader(onAddClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "История",
            color = CustomGrey,
            fontFamily = segoe_ui_bold,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp
        )
        IconButton(onClick = onAddClick) {
            Icon(
                painter = painterResource(id = R.drawable.vector_add),
                contentDescription = "addRefuelingIcon",
                tint = CustomTurquoiseBlue,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

// ===================== Сводка текущего месяца =====================

@Composable
private fun MonthSummaryRow(
    monthLabel: String,
    totalSum: Double,
    totalVolume: Double,
    savings: Double,
    visits: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Крупная карточка слева — расходы за месяц
        Column(
            modifier = Modifier
                .weight(1.3f)
                .clip(RoundedCornerShape(16.dp))
                .border(2.5.dp, CustomTurquoiseBlue, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Text(
                text = if (monthLabel.isNotBlank()) "Расходы в ${monthLabel.lowercase()}" else "Расходы",
                color = CustomCarpiBlue,
                fontFamily = segoe_ui_bold,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "${formatMoney(totalSum)} ₽",
                color = CustomGrey,
                fontFamily = segoe_ui_bold,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${formatVolume(totalVolume)} л.",
                color = CustomGrey.copy(alpha = 0.7f),
                fontFamily = segoe_ui,
                fontSize = 13.sp
            )
        }

        // Две маленькие карточки справа — экономия и визиты
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SmallStatCard(title = "Экономия в этом месяце", value = "+ ${formatMoney(savings)} ₽", valueColor = CustomTurquoiseBlue)
            SmallStatCard(title = "Количество визитов", value = "$visits", valueColor = CustomGrey)
        }
    }
}

@Composable
private fun SmallStatCard(title: String, value: String, valueColor: androidx.compose.ui.graphics.Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(2.5.dp, CustomTurquoiseBlue, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Text(
            text = title,
            color = CustomCarpiBlue,
            fontFamily = segoe_ui_bold,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            lineHeight = 13.sp
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            color = valueColor,
            fontFamily = segoe_ui_bold,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

private fun formatMoney(value: Double): String = "%,.0f".format(value).replace(',', ' ')
private fun formatVolume(value: Double): String = "%.1f".format(value)

// ========================= Фильтры (чипы) =========================

@Composable
private fun FilterChipsRow(
    uiState: HistoryScreenUIState,
    onOnlyFuelCardChanged: (Boolean) -> Unit,
    onSortByChanged: (SortBy) -> Unit,
    onVehicleFilterChanged: (String?) -> Unit,
    onPeriodFilterChanged: (PeriodFilter) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        //TODO: добавить фильтр-иконку
        item { WhatToShowChip(uiState, onOnlyFuelCardChanged, onSortByChanged) }
        item { VehicleFilterChip(uiState, onVehicleFilterChanged) }
        item { GasStationFilterChip(uiState.selectedGasStationLabel) }
        item { PeriodFilterChip(uiState.periodFilter, onPeriodFilterChanged) }
    }
}

/**
 * Общая "оболочка" чипа-фильтра: пилюля с текстом + стрелкой, по клику
 * открывает DropdownMenu. [dropdownContent] получает лямбду close(),
 * которую нужно вызвать после выбора пункта, чтобы меню закрылось.
 */
@Composable
private fun FilterChipBase(
    label: String,
    dropdownContent: @Composable ColumnScope.(close: () -> Unit) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .border(1.dp, CustomTurquoiseBlue, RoundedCornerShape(50))
                .background(CustomTrafficWhite)
                .clickable { expanded = true }
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = CustomGrey, fontFamily = segoe_ui, fontSize = 13.sp)
            Spacer(Modifier.width(4.dp))
            Text("⌄", color = CustomGrey, fontSize = 13.sp)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(CustomTrafficWhite)
        ) {
            dropdownContent { expanded = false }
        }
    }
}

@Composable
private fun WhatToShowChip(
    uiState: HistoryScreenUIState,
    onOnlyFuelCardChanged: (Boolean) -> Unit,
    onSortByChanged: (SortBy) -> Unit
) {
    val label = when {
        uiState.onlyFuelCard -> "По топливной карте"
        uiState.sortBy != SortBy.DATE -> "Сорт: ${uiState.sortBy.name}"
        else -> "Что показывать"
    }
    FilterChipBase(label = label) { close ->
        DropdownMenuItem(
            text = { Text("Все заправки", color = CustomGrey, fontSize = 13.sp) },
            onClick = { onOnlyFuelCardChanged(false); close() }
        )
        DropdownMenuItem(
            text = { Text("Только по топливной карте", color = CustomGrey, fontSize = 13.sp) },
            onClick = { onOnlyFuelCardChanged(true); close() }
        )
        HorizontalDivider(color = CustomGrey.copy(alpha = 0.2f))
        Text(
            "Сортировка",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            fontWeight = FontWeight.Bold,
            color = CustomGrey,
            fontSize = 11.sp
        )
        DropdownMenuItem(
            text = { Text("По дате", color = CustomGrey, fontSize = 13.sp) },
            onClick = { onSortByChanged(SortBy.DATE); close() }
        )
        DropdownMenuItem(
            text = { Text("По сумме", color = CustomGrey, fontSize = 13.sp) },
            onClick = { onSortByChanged(SortBy.SUM); close() }
        )
        DropdownMenuItem(
            text = { Text("По объёму", color = CustomGrey, fontSize = 13.sp) },
            onClick = { onSortByChanged(SortBy.VOLUME); close() }
        )
    }
}

@Composable
private fun VehicleFilterChip(uiState: HistoryScreenUIState, onVehicleFilterChanged: (String?) -> Unit) {
    val label = uiState.userVehicles
        .find { it.vehicleId == uiState.selectedVehicleId }
        ?.let { it.licensePlate ?: it.name }
        ?: "Автомобиль"

    FilterChipBase(label = label) { close ->
        DropdownMenuItem(
            text = { Text("Все автомобили", color = CustomGrey, fontSize = 13.sp) },
            onClick = { onVehicleFilterChanged(null); close() }
        )
        uiState.userVehicles.forEach { vehicle ->
            DropdownMenuItem(
                text = { Text(vehicle.licensePlate ?: vehicle.name, color = CustomGrey, fontSize = 13.sp) },
                onClick = { onVehicleFilterChanged(vehicle.vehicleId); close() }
            )
        }
    }
}

@Composable
private fun GasStationFilterChip(currentLabel: String) {
    // TODO: когда на проекте появится больше одной станции — подключить
    // реальный список АЗС с бэкенда вместо единственного статичного пункта.
    FilterChipBase(label = currentLabel) { close ->
        DropdownMenuItem(
            text = { Text("Все АЗС", color = CustomGrey, fontSize = 13.sp) },
            onClick = { close() }
        )
    }
}

@Composable
private fun PeriodFilterChip(current: PeriodFilter, onPeriodFilterChanged: (PeriodFilter) -> Unit) {
    FilterChipBase(label = current.label) { close ->
        PeriodFilter.values().forEach { period ->
            DropdownMenuItem(
                text = { Text(period.label, color = CustomGrey, fontSize = 13.sp) },
                onClick = { onPeriodFilterChanged(period); close() }
            )
        }
    }
}

// ========================= Карточка месяца =========================

@Composable
private fun MonthCard(monthLabel: String, totalSum: Double, entries: List<RefuelingDto>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(18.dp))
            .border(2.5.dp, CustomTurquoiseBlue, RoundedCornerShape(18.dp))
            .padding(18.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(monthLabel, color = CustomGrey, fontFamily = segoe_ui_bold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("${formatMoney(totalSum)} р.", color = CustomGrey, fontFamily = segoe_ui_bold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Spacer(Modifier.height(14.dp))
        entries.forEachIndexed { index, entry ->
            HistoryRow(entry)
            if (index != entries.lastIndex) Spacer(Modifier.height(14.dp))
        }
    }
}

@Composable
private fun HistoryRow(entry: RefuelingDto) {
    val displayDate = remember(entry.refuelDate) { formatShortDate(entry.refuelDate) }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(displayDate, color = CustomGrey, fontFamily = segoe_ui, fontSize = 13.sp)
        Text("${formatVolume(entry.volume)} л.", color = CustomGrey, fontFamily = segoe_ui, fontSize = 13.sp)
        Text("${formatMoney(entry.totalSum)} р.", color = CustomGrey, fontFamily = segoe_ui, fontSize = 13.sp)
    }
}

private fun formatShortDate(raw: String): String {
    return try {
        val date = LocalDate.parse(raw.substringBefore('T'))
        date.format(DateTimeFormatter.ofPattern("dd.MM.yy"))
    } catch (e: Exception) {
        raw
    }
}

// =================== Bottom sheet "Добавить заправку" ===================

@Composable
private fun AddRefuelSheetContent(
    date: String,
    volume: String,
    sum: String,
    isSubmitting: Boolean,
    onDateChanged: (String) -> Unit,
    onVolumeChanged: (String) -> Unit,
    onSumChanged: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 8.dp, bottom = 32.dp)
    ) {
        Text(
            text = "Добавить заправку",
            color = CustomGrey,
            fontFamily = segoe_ui_bold,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp
        )
        Spacer(Modifier.height(24.dp))

        AddRefuelField(value = date, onValueChange = onDateChanged, placeholder = "укажите дату: дд.мм.гггг")
        AddRefuelField(value = volume, onValueChange = onVolumeChanged, placeholder = "укажите объем", keyboardType = KeyboardType.Decimal)
        AddRefuelField(value = sum, onValueChange = onSumChanged, placeholder = "укажите сумму", keyboardType = KeyboardType.Decimal)

        Spacer(Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(50))
                .background(CustomTurquoiseBlue)
                .clickable(enabled = !isSubmitting, onClick = onSubmit),
            contentAlignment = Alignment.Center
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    color = CustomTrafficWhite,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Text("Добавить", color = CustomTrafficWhite, fontFamily = segoe_ui, fontSize = 18.sp)
            }
        }
    }
}

@Composable
private fun AddRefuelField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(50))
                .background(CustomEnterBarColor),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                textStyle = TextStyle(color = CustomGrey, fontSize = 14.sp, fontFamily = segoe_ui),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(placeholder, color = CustomGrey.copy(alpha = 0.6f), fontSize = 14.sp, fontFamily = segoe_ui)
                    }
                    innerTextField()
                }
            )
        }
        Spacer(Modifier.height(10.dp))
        HorizontalDivider(color = CustomTurquoiseBlue, thickness = 1.5.dp)
        Spacer(Modifier.height(16.dp))
    }
}
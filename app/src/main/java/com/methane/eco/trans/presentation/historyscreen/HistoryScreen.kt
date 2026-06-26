package com.methane.eco.trans.presentation.historyscreen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.methane.eco.trans.R
import com.methane.eco.trans.data.local.TokenStorage
import com.methane.eco.trans.data.repository.MainRepositoryImpl
import com.methane.eco.trans.domain.usecase.GetRefuelingHistoryUseCase
import com.methane.eco.trans.domain.usecase.GetVehiclesUseCase
import com.methane.eco.trans.presentation.viewmodel.HistoryViewModel
import com.methane.eco.trans.data.dto.RefuelingDto
import com.methane.eco.trans.segoe_ui
import com.methane.eco.trans.theme.CustomDeepOrange
import com.methane.eco.trans.theme.CustomGrey
import com.methane.eco.trans.theme.CustomTrafficWhite
import com.methane.eco.trans.theme.CustomTurquoiseBlue

@Composable
fun HistoryScreen(
    navController: NavController,
    viewModel: HistoryViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val context = navController.context
                val tokenStorage = TokenStorage(context)
                val repository = MainRepositoryImpl(tokenStorage)
                val getVehiclesUseCase = GetVehiclesUseCase(repository)
                val getRefuelingHistoryUseCase = GetRefuelingHistoryUseCase(repository)
                return HistoryViewModel(getVehiclesUseCase, getRefuelingHistoryUseCase) as T
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
            }
        }
    }

    // Группировка истории по месяцам
    val groupedHistory = remember(uiState.history) {
        uiState.history.groupBy { refueling ->
            try {
                val dateStr = refueling.refuelDate.substringBefore('T')
                val parts = dateStr.split("-")
                val year = parts[0].toInt()
                val month = parts[1].toInt()
                "$month;$year"
            } catch (e: Exception) { "0;0" }
        }.toSortedMap(compareByDescending {
            val parts = it.split(";")
            parts[1].toInt() * 100 + parts[0].toInt()
        })
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(color = CustomTurquoiseBlue)
    ) {
        val boxWidth = this.maxWidth
        val boxHeight = this.maxHeight

        // Поле "История"
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = boxWidth / 10 - 12.dp,
                    end = boxWidth / 10 * 5 + 12.dp,
                    top = boxHeight / 18 * 1 - 12.dp,
                    bottom = boxHeight / 18 * 16 + 12.dp
                )
        ) {
            Box(
                modifier = Modifier
                    .size(boxWidth / 10 * 4, boxHeight / 18 * 1)
                    .background(color = CustomTurquoiseBlue, shape = RoundedCornerShape(15.dp))
                    .border(1.dp, CustomTrafficWhite, RoundedCornerShape(15.dp))
            ) {
                Text(
                    text = "История",
                    modifier = Modifier.align(Alignment.Center),
                    color = CustomTrafficWhite,
                    fontFamily = segoe_ui,
                    fontSize = 16.sp
                )
            }
        }

        // ✅ НОВОЕ МЕНЮ ФИЛЬТРОВ И СОРТИРОВКИ
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = boxWidth / 10 * 5 + 12.dp,
                    end = boxWidth / 10 * 1 - 12.dp,
                    top = boxHeight / 18 * 1 - 12.dp,
                    bottom = boxHeight / 18 * 16 + 12.dp
                )
        ) {
            var expanded by remember { mutableStateOf(false) }

            // Формируем текст кнопки в зависимости от выбранных фильтров
            val buttonText = when {
                uiState.selectedVehicleId != null -> {
                    val vehicle = uiState.userVehicles.find { it.vehicleId == uiState.selectedVehicleId }
                    vehicle?.licensePlate ?: vehicle?.name ?: "Фильтры ≡"
                }
                uiState.onlyFuelCard -> "По топливным картам ≡"
                uiState.sortBy != SortBy.DATE -> "Сорт: ${uiState.sortBy.name} ≡"
                else -> "Все авто ≡"
            }

            Box(
                modifier = Modifier
                    .size(boxWidth / 10 * 4, boxHeight / 18 * 1)
                    .background(color = CustomTurquoiseBlue, shape = RoundedCornerShape(15.dp))
                    .border(1.dp, CustomTrafficWhite, RoundedCornerShape(15.dp))
                    .clickable { expanded = true }
            ) {
                Text(
                    text = buttonText,
                    modifier = Modifier.align(Alignment.Center),
                    color = CustomTrafficWhite,
                    fontFamily = segoe_ui,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .width(boxWidth / 10 * 4)
                        .background(CustomTrafficWhite)
                ) {
                    // Секция: Автомобиль
                    Text(
                        text = "Автомобиль",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold,
                        color = CustomGrey,
                        fontSize = 12.sp
                    )
                    DropdownMenuItem(
                        text = { Text("Все автомобили", color = CustomGrey, fontSize = 12.sp) },
                        onClick = { viewModel.onVehicleFilterChanged(null) }
                    )
                    uiState.userVehicles.forEach { vehicle ->
                        DropdownMenuItem(
                            text = { Text(vehicle.licensePlate ?: vehicle.name, color = CustomGrey, fontSize = 12.sp) },
                            onClick = { viewModel.onVehicleFilterChanged(vehicle.vehicleId) }
                        )
                    }

                    HorizontalDivider(color = CustomGrey.copy(alpha = 0.2f))

                    // Секция: Сортировка
                    Text(
                        text = "Сортировка",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold,
                        color = CustomGrey,
                        fontSize = 12.sp
                    )
                    DropdownMenuItem(text = { Text("По дате", color = CustomGrey, fontSize = 12.sp) }, onClick = { viewModel.onSortByChanged(SortBy.DATE) })
                    DropdownMenuItem(text = { Text("По сумме", color = CustomGrey, fontSize = 12.sp) }, onClick = { viewModel.onSortByChanged(SortBy.SUM) })
                    DropdownMenuItem(text = { Text("По объему", color = CustomGrey, fontSize = 12.sp) }, onClick = { viewModel.onSortByChanged(SortBy.VOLUME) })

                    HorizontalDivider(color = CustomGrey.copy(alpha = 0.2f))

                    // Секция: Доп. Фильтры
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.onOnlyFuelCardChanged(!uiState.onlyFuelCard) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = uiState.onlyFuelCard,
                            onCheckedChange = { viewModel.onOnlyFuelCardChanged(it) },
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Только по топливной карте", color = CustomGrey, fontSize = 12.sp)
                    }
                }
            }
        }

        // Внешнее поле с прокруткой (Список истории)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = boxWidth / 10 - 12.dp,
                    end = boxWidth / 10 * 1 - 12.dp,
                    top = boxHeight / 18 * 2 + 12.dp,
                    bottom = boxHeight / 18 * 3 - 12.dp
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = CustomTrafficWhite, shape = RoundedCornerShape(15.dp))
                    .border(1.dp, CustomDeepOrange, shape = RoundedCornerShape(15.dp)),
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (groupedHistory.isEmpty() && uiState.history.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    Text(
                                        text = "Нет данных о заправках",
                                        color = CustomGrey,
                                        fontFamily = segoe_ui,
                                        fontSize = 18.sp,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                            }
                        } else {
                            groupedHistory.forEach { (monthYear, records) ->
                                val (monthStr, yearStr) = monthYear.split(";")
                                val month = monthStr.toIntOrNull() ?: 0
                                val year = yearStr.toIntOrNull() ?: 0

                                // Заголовок месяца
                                item {
                                    val monthName = when(month) {
                                        1 -> "Январь"; 2 -> "Февраль"; 3 -> "Март"; 4 -> "Апрель"
                                        5 -> "Май"; 6 -> "Июнь"; 7 -> "Июль"; 8 -> "Август"
                                        9 -> "Сентябрь"; 10 -> "Октябрь"; 11 -> "Ноябрь"; 12 -> "Декабрь"
                                        else -> "Месяц"
                                    }
                                    Text(
                                        text = "$monthName $year",
                                        color = CustomGrey,
                                        fontFamily = segoe_ui,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }

                                // Записи заправок
                                items(records) { refueling ->
                                    RefuelingItem(refueling = refueling, boxWidth = boxWidth)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Поле с навигацией
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = boxWidth / 10 - 12.dp,
                    end = boxWidth / 10 * 1 - 12.dp,
                    top = boxHeight / 18 * 16 + 12.dp,
                    bottom = boxHeight / 18 * 1 - 12.dp
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = CustomTrafficWhite, shape = RoundedCornerShape(15.dp))
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(15.dp),
                    border = BorderStroke(1.dp, CustomDeepOrange),
                    color = CustomTrafficWhite
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(
                            R.drawable.vector_telegram to "contacts",
                            R.drawable.vector_profile to "profile",
                            R.drawable.vector_home to "main"
                        ).forEach { (iconRes, description) ->
                            Box(
                                modifier = Modifier.size(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                androidx.compose.foundation.Image(
                                    painter = painterResource(id = iconRes),
                                    contentDescription = "${description}Icon",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable {
                                            when (description) {
                                                "profile" -> viewModel.onProfileClicked()
                                                "main" -> viewModel.onMainClicked()
                                            }
                                        }
                                )
                            }
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

// ✅ Вынесенный компонент для красивой отрисовки одной заправки
@Composable
fun RefuelingItem(refueling: RefuelingDto, boxWidth: androidx.compose.ui.unit.Dp) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        // Основная строка: Дата | Объем | Сумма
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val displayDate = try {
                refueling.refuelDate.substringBefore('T').split("-").reversed().joinToString(".")
            } catch (e: Exception) { refueling.refuelDate }

            Text(text = displayDate, color = CustomGrey, fontFamily = segoe_ui, fontSize = 12.sp)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = "${refueling.volume} л", color = CustomGrey, fontFamily = segoe_ui, fontSize = 12.sp)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = "${refueling.totalSum} ₽", color = CustomGrey, fontFamily = segoe_ui, fontSize = 12.sp)
        }

        // Дополнительная строка (мелким шрифтом): Топливная карта | Адрес АЗС
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Слева: Топливная карта
            val cardText = if (refueling.fuelCardId != null) {
                "Карта: ...${refueling.fuelCardId.takeLast(4)}"
            } else {
                "Наличные/Карта"
            }
            Text(
                text = cardText,
                color = CustomGrey.copy(alpha = 0.6f),
                fontFamily = segoe_ui,
                fontSize = 10.sp
            )

            // Справа: Адрес АЗС
            Text(
                text = refueling.gasStationAddress,
                color = CustomGrey.copy(alpha = 0.6f),
                fontFamily = segoe_ui,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = boxWidth / 2) // Ограничиваем ширину, чтобы не ломало верстку
            )
        }

        // Разделитель для визуального отделения записей
        HorizontalDivider(
            modifier = Modifier.padding(top = 6.dp),
            thickness = 0.5.dp,
            color = CustomGrey.copy(alpha = 0.2f)
        )
    }
}
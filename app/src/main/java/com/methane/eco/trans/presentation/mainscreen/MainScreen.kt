package com.methane.eco.trans.presentation.mainscreen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.methane.eco.trans.R
import com.methane.eco.trans.data.dto.VehicleDto
import com.methane.eco.trans.data.local.TokenStorage
import com.methane.eco.trans.data.repository.MainRepositoryImpl
import com.methane.eco.trans.domain.usecase.AddRefuelingUseCase
import com.methane.eco.trans.domain.usecase.AddVehicleUseCase
import com.methane.eco.trans.domain.usecase.DeleteVehicleUseCase
import com.methane.eco.trans.domain.usecase.GetRefuelingHistoryUseCase
import com.methane.eco.trans.domain.usecase.GetVehiclesUseCase
import com.methane.eco.trans.isDateValid
import com.methane.eco.trans.presentation.viewmodel.MainScreenViewModel
import com.methane.eco.trans.segoe_ui
import com.methane.eco.trans.theme.CustomCarpiBlue
import com.methane.eco.trans.theme.CustomDeepOrange
import com.methane.eco.trans.theme.CustomEnterBarColor
import com.methane.eco.trans.theme.CustomErrorBarBackgroundColor
import com.methane.eco.trans.theme.CustomGrey
import com.methane.eco.trans.theme.CustomTrafficWhite
import com.methane.eco.trans.theme.CustomTurquoiseBlue
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun MainScreen(
    navController: NavController,
    viewModel: MainScreenViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val context = navController.context
                val tokenStorage = TokenStorage(context)
                val repository = MainRepositoryImpl(tokenStorage)

                val getVehiclesUseCase = GetVehiclesUseCase(repository)
                val addVehicleUseCase = AddVehicleUseCase(repository)
                val deleteVehicleUseCase = DeleteVehicleUseCase(repository)
                val addRefuelingUseCase = AddRefuelingUseCase(repository)
                val getRefuelingHistoryUseCase = GetRefuelingHistoryUseCase(repository)

                return MainScreenViewModel(
                    getVehiclesUseCase,
                    addVehicleUseCase,
                    deleteVehicleUseCase,
                    addRefuelingUseCase,
                    getRefuelingHistoryUseCase
                ) as T
            }
        }
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Обработка событий
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is MainScreenEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is MainScreenEvent.NavigateToHistoryScreen -> {
                    navController.navigate("HistoryScreen")
                }
                is MainScreenEvent.NavigateToProfileScreen -> {
                    navController.navigate("ProfileScreen")
                }
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(color = CustomTrafficWhite)
    ) {
        val boxWidth = this.maxWidth
        val boxHeight = this.maxHeight

        // Кнопка добавить заправку
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = boxWidth / 10,
                    end = boxWidth / 10,
                    top = boxHeight / 18 * 9 - 12.dp,
                    bottom = boxHeight / 18 * 7
                )
        ) {
            Box(
                modifier = Modifier
                    .size(boxWidth / 10 * 8, boxHeight / 18 * 1)
                    .background(color = CustomCarpiBlue, shape = RoundedCornerShape(15.dp))
                    .clickable { viewModel.onShowRefuelDialogChanged(true) }
            ) {
                Text(
                    text = "Добавить заправку",
                    modifier = Modifier.align(Alignment.Center),
                    color = CustomTrafficWhite,
                    fontFamily = segoe_ui,
                    fontSize = 16.sp
                )
            }
        }

        // Диалог добавления заправки
        if (uiState.showRefuelDialog) {
            Dialog(
                onDismissRequest = { viewModel.onShowRefuelDialogChanged(false) },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    dismissOnClickOutside = true
                )
            ) {
                Column(
                    modifier = Modifier
                        .width(320.dp)
                        .background(CustomTrafficWhite, RoundedCornerShape(15.dp))
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // БЛОК 1: ДОБАВЛЕНИЕ ЗАПРАВКИ
                    Text(
                        text = "Новая заправка",
                        color = CustomCarpiBlue,
                        fontFamily = segoe_ui,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Выбор автомобиля
                    VehicleDropdown(
                        vehicles = uiState.userVehicles,
                        selectedId = uiState.currentVehicleId,
                        onSelected = { viewModel.onCurrentVehicleIdChanged(it) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Дата и Объем
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DialogTextField(
                            value = uiState.date,
                            onValueChange = { viewModel.onDateChanged(it) },
                            placeholder = "дд.мм.гггг",
                            modifier = Modifier.weight(1f)
                        )
                        DialogTextField(
                            value = uiState.volume,
                            onValueChange = { viewModel.onVolumeChanged(it) },
                            placeholder = "Объем (л)",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Сумма и Топливная карта
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DialogTextField(
                            value = uiState.sum,
                            onValueChange = { viewModel.onSumChanged(it) },
                            placeholder = "Сумма (₽)",
                            modifier = Modifier.weight(1f)
                        )
                        DialogTextField(
                            value = uiState.fuelCardNumber,
                            onValueChange = { viewModel.onFuelCardChanged(it) },
                            placeholder = "№ Карты (опц.)",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Кнопка сохранения заправки
                    DialogButton(
                        text = "Добавить заправку",
                        isLoading = uiState.isLoading,
                        onClick = {
                            if (uiState.date.isBlank() || uiState.volume.isBlank() || uiState.sum.isBlank()) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Заполните обязательные поля")
                                }
                                return@DialogButton
                            }
                            if (!isDateValid(uiState.date)) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Некорректный формат даты")
                                }
                                return@DialogButton
                            }
                            if (uiState.currentVehicleId.isBlank()) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Выберите автомобиль")
                                }
                                return@DialogButton
                            }
                            val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                            val localDate = LocalDate.parse(uiState.date, dateFormatter)
                            val isoDate = localDate.atStartOfDay().toString()  // "2026-06-25T00:00:00"

                            viewModel.addRefueling()
                        }
                    )

                    // Разделитель
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        thickness = 1.dp,
                        color = CustomGrey.copy(alpha = 0.2f)
                    )

                    // БЛОК 2: УПРАВЛЕНИЕ АВТОПАРКОМ
                    Text(
                        text = "Мои автомобили",
                        color = CustomCarpiBlue,
                        fontFamily = segoe_ui,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Поле ввода и кнопки +/-
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DialogTextField(
                            value = uiState.newVehiclePlate,
                            onValueChange = { viewModel.onNewVehiclePlateChanged(it) },
                            placeholder = "Номер авто",
                            modifier = Modifier.weight(1f)
                        )

                        // Кнопка +
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(CustomCarpiBlue, RoundedCornerShape(8.dp))
                                .clickable { viewModel.addNewVehicle() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "＋",
                                color = CustomTrafficWhite,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Кнопка -
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(CustomErrorBarBackgroundColor, RoundedCornerShape(8.dp))
                                .clickable { viewModel.deleteVehicleByPlate() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "－",
                                color = CustomTrafficWhite,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Список автомобилей
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 150.dp)
                            .background(CustomEnterBarColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    ) {
                        if (uiState.userVehicles.isEmpty()) {
                            Text(
                                text = "Список пуст",
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(8.dp),
                                color = CustomGrey.copy(alpha = 0.6f),
                                fontSize = 12.sp
                            )
                        } else {
                            LazyColumn(modifier = Modifier.padding(8.dp)) {
                                items(uiState.userVehicles) { vehicle ->
                                    Text(
                                        text = vehicle.licensePlate ?: vehicle.name,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clickable {
                                                viewModel.onCurrentVehicleIdChanged(vehicle.vehicleId)
                                            },
                                        color = if (vehicle.vehicleId == uiState.currentVehicleId)
                                            CustomCarpiBlue else CustomGrey,
                                        fontWeight = if (vehicle.vehicleId == uiState.currentVehicleId)
                                            FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 14.sp
                                    )
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
                    top = boxHeight / 18 * 16,
                    bottom = boxHeight / 18 * 1 - 12.dp
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        horizontal = boxWidth / 10,
                        vertical = 12.dp
                    )
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(15.dp),
                    border = BorderStroke(1.dp, CustomDeepOrange),
                    color = CustomTrafficWhite
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(
                            R.drawable.vector_telegram to "contacts",
                            R.drawable.vector_history to "history",
                            R.drawable.vector_profile to "profile"
                        ).forEach { (iconRes, description) ->
                            Box(
                                modifier = Modifier.size(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = iconRes),
                                    contentDescription = "${description}Icon",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable {
                                            when (description) {
                                                "profile" -> viewModel.onProfileClicked()
                                                "history" -> viewModel.onHistoryClicked()
                                            }
                                        },
                                    tint = CustomGrey
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

// --- Вспомогательные компоненты для Диалога ---

@Composable
fun DialogTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .height(40.dp)
            .background(CustomEnterBarColor, RoundedCornerShape(8.dp))
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .onFocusChanged { focusState -> isFocused = focusState.isFocused },
            textStyle = TextStyle(
                color = CustomGrey,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            ),
            singleLine = true,
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (value.isEmpty() && !isFocused) {
                        Text(
                            placeholder,
                            color = CustomGrey.copy(alpha = 0.5f),
                            fontSize = 12.sp,
                            fontFamily = segoe_ui
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

@Composable
fun DialogButton(
    text: String,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(CustomCarpiBlue, RoundedCornerShape(8.dp))
            .clickable(enabled = !isLoading, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = CustomTrafficWhite,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text,
                color = CustomTrafficWhite,
                fontFamily = segoe_ui,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun VehicleDropdown(
    vehicles: List<VehicleDto>,
    selectedId: String,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val selectedName = vehicles.find { it.vehicleId == selectedId }?.let {
        it.licensePlate ?: it.name
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(CustomTurquoiseBlue, RoundedCornerShape(8.dp))
            .clickable {
                focusManager.clearFocus()
                expanded = true
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = selectedName ?: "Выберите автомобиль ",
            color = CustomTrafficWhite,
            fontFamily = segoe_ui,
            fontSize = 14.sp
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(CustomTurquoiseBlue)
        ) {
            if (vehicles.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("Нет авто", color = CustomTrafficWhite) },
                    onClick = { expanded = false }
                )
            } else {
                vehicles.forEach { v ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                v.licensePlate ?: v.name,
                                color = CustomTrafficWhite
                            )
                        },
                        onClick = {
                            onSelected(v.vehicleId)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
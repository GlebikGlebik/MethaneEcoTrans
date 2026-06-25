package com.methane.eco.trans.presentation.mainscreen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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

        // Состояния для фокуса
        var isFocusedDate by remember { mutableStateOf(false) }
        var isFocusedVolume by remember { mutableStateOf(false) }
        var isFocusedSum by remember { mutableStateOf(false) }
        var isFocusedNewVehicle by remember { mutableStateOf(false) }

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
                BoxWithConstraints(
                    modifier = Modifier
                        .background(
                            color = CustomTrafficWhite,
                            shape = RoundedCornerShape(15.dp)
                        )
                ) {
                    val dialogWidth = this.maxWidth * 0.5f
                    val dialogHeight = this.maxHeight * 0.5f

                    Box(
                        modifier = Modifier
                            .requiredSize(dialogWidth, dialogHeight)
                            .background(CustomTrafficWhite)
                    ) {
                        // Поле выбора авто
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(
                                    top = dialogHeight / 21 * 1,
                                    start = dialogWidth / 11 * 3,
                                    end = dialogWidth / 11 * 3,
                                    bottom = dialogHeight / 21 * 9
                                )
                        ) {
                            var expanded by remember { mutableStateOf(false) }
                            val focusManager = LocalFocusManager.current

                            Box(
                                modifier = Modifier
                                    .background(CustomTurquoiseBlue, shape = RoundedCornerShape(15.dp))
                                    .requiredSize(dialogWidth / 11 * 5, dialogHeight / 21 * 1)
                                    .clickable {
                                        focusManager.clearFocus()
                                        expanded = true
                                    }
                            ) {
                                Text(
                                    text = uiState.currentVehicle.ifEmpty { "Номер авто ≡" },
                                    modifier = Modifier.align(Alignment.Center),
                                    color = CustomTrafficWhite,
                                    fontFamily = segoe_ui,
                                    fontSize = 12.sp
                                )

                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false },
                                    modifier = Modifier
                                        .width(dialogWidth / 11 * 5)
                                        .background(CustomTurquoiseBlue)
                                ) {
                                    if (uiState.userVehicles.isEmpty()) {
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    "Добавьте автомобиль",
                                                    color = CustomTrafficWhite
                                                )
                                            },
                                            onClick = { expanded = false }
                                        )
                                    } else {
                                        uiState.userVehicles.forEach { vehicle ->
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        vehicle.licensePlate ?: vehicle.name,
                                                        color = CustomTrafficWhite
                                                    )
                                                },
                                                onClick = {
                                                    viewModel.onCurrentVehicleChanged(vehicle.vehicleId)
                                                    expanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Поле ввода даты
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(
                                    top = dialogHeight / 21 * 3,
                                    start = dialogWidth / 11 * 1,
                                    end = dialogWidth / 11 * 1,
                                    bottom = dialogHeight / 21 * 7
                                )
                        ) {
                            Box(
                                modifier = Modifier
                                    .requiredSize(dialogWidth / 11 * 9, dialogHeight / 21 * 1)
                                    .background(CustomEnterBarColor, shape = RoundedCornerShape(15.dp))
                            ) {
                                BasicTextField(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .align(Alignment.Center)
                                        .onFocusChanged { focusState -> isFocusedDate = focusState.isFocused },
                                    value = uiState.date,
                                    onValueChange = { newText ->
                                        if (newText.length <= 10) {
                                            viewModel.onDateChanged(newText)
                                        }
                                    },
                                    textStyle = TextStyle(
                                        color = CustomGrey,
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center
                                    ),
                                    cursorBrush = Brush.verticalGradient(
                                        colors = listOf(
                                            CustomGrey.copy(alpha = 0.5f),
                                            CustomGrey.copy(alpha = 0.5f)
                                        ),
                                        startY = 0f,
                                        endY = 12f
                                    ),
                                    decorationBox = { innerTextField ->
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (uiState.date.isEmpty() && !isFocusedDate) {
                                                Text(
                                                    text = "дата: xx.xx.xxxx",
                                                    modifier = Modifier.alpha(0.5f),
                                                    color = CustomGrey,
                                                    fontFamily = segoe_ui,
                                                    fontSize = 12.sp,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                            innerTextField()
                                        }
                                    }
                                )
                            }
                        }

                        // Поле ввода объема
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(
                                    top = dialogHeight / 21 * 5,
                                    start = dialogWidth / 11 * 1,
                                    end = dialogWidth / 11 * 1,
                                    bottom = dialogHeight / 21 * 5
                                )
                        ) {
                            Box(
                                modifier = Modifier
                                    .requiredSize(dialogWidth / 11 * 9, dialogHeight / 21 * 1)
                                    .background(CustomEnterBarColor, shape = RoundedCornerShape(15.dp))
                            ) {
                                BasicTextField(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .align(Alignment.Center)
                                        .onFocusChanged { focusState -> isFocusedVolume = focusState.isFocused },
                                    value = uiState.volume,
                                    onValueChange = { newText ->
                                        if (newText.length <= 10) {
                                            viewModel.onVolumeChanged(newText)
                                        }
                                    },
                                    textStyle = TextStyle(
                                        color = CustomGrey,
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center
                                    ),
                                    cursorBrush = Brush.verticalGradient(
                                        colors = listOf(
                                            CustomGrey.copy(alpha = 0.5f),
                                            CustomGrey.copy(alpha = 0.5f)
                                        ),
                                        startY = 0f,
                                        endY = 12f
                                    ),
                                    decorationBox = { innerTextField ->
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (uiState.volume.isEmpty() && !isFocusedVolume) {
                                                Text(
                                                    text = "объем",
                                                    modifier = Modifier.alpha(0.5f),
                                                    color = CustomGrey,
                                                    fontFamily = segoe_ui,
                                                    fontSize = 12.sp,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                            innerTextField()
                                        }
                                    }
                                )
                            }
                        }

                        // Поле ввода суммы
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(
                                    top = dialogHeight / 21 * 7,
                                    start = dialogWidth / 11 * 1,
                                    end = dialogWidth / 11 * 1,
                                    bottom = dialogHeight / 21 * 3
                                )
                        ) {
                            Box(
                                modifier = Modifier
                                    .requiredSize(dialogWidth / 11 * 9, dialogHeight / 21 * 1)
                                    .background(CustomEnterBarColor, shape = RoundedCornerShape(15.dp))
                            ) {
                                BasicTextField(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .align(Alignment.Center)
                                        .onFocusChanged { focusState -> isFocusedSum = focusState.isFocused },
                                    value = uiState.sum,
                                    onValueChange = { newText ->
                                        if (newText.length <= 10) {
                                            viewModel.onSumChanged(newText)
                                        }
                                    },
                                    textStyle = TextStyle(
                                        color = CustomGrey,
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center
                                    ),
                                    cursorBrush = Brush.verticalGradient(
                                        colors = listOf(
                                            CustomGrey.copy(alpha = 0.5f),
                                            CustomGrey.copy(alpha = 0.5f)
                                        ),
                                        startY = 0f,
                                        endY = 12f
                                    ),
                                    decorationBox = { innerTextField ->
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (uiState.sum.isEmpty() && !isFocusedSum) {
                                                Text(
                                                    text = "сумма",
                                                    modifier = Modifier.alpha(0.5f),
                                                    color = CustomGrey,
                                                    fontFamily = segoe_ui,
                                                    fontSize = 12.sp,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                            innerTextField()
                                        }
                                    }
                                )
                            }
                        }

                        // Кнопка добавить заправку
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(
                                    top = dialogHeight / 21 * 9 - 4.dp,
                                    start = dialogWidth / 11 * 1,
                                    end = dialogWidth / 11 * 1,
                                    bottom = dialogHeight / 21 * 1 + 4.dp
                                )
                        ) {
                            Box(
                                modifier = Modifier
                                    .requiredSize(dialogWidth / 11 * 9, dialogHeight / 21 * 1)
                                    .background(CustomCarpiBlue, shape = RoundedCornerShape(15.dp))
                                    .clickable {
                                        // Валидация и отправка
                                        if (uiState.date.isBlank() || uiState.volume.isBlank() || uiState.sum.isBlank()) {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Заполните все поля")
                                            }
                                            return@clickable
                                        }
                                        if (!isDateValid(uiState.date)) {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Некорректный формат даты")
                                            }
                                            return@clickable
                                        }
                                        if (uiState.currentVehicle.isBlank()) {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Выберите ТС")
                                            }
                                            return@clickable
                                        }

                                        // Преобразование даты в ISO формат
                                        val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                                        val localDate = LocalDate.parse(uiState.date, dateFormatter)
                                        val isoDate = localDate.atStartOfDay().toString()

                                        viewModel.addRefueling(
                                            vehicleId = uiState.currentVehicle,
                                            volume = uiState.volume.toDoubleOrNull() ?: 0.0,
                                            totalSum = uiState.sum.toDoubleOrNull() ?: 0.0,
                                            refuelDate = isoDate
                                        )
                                        viewModel.onShowRefuelDialogChanged(false)
                                    }
                            ) {
                                if (uiState.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .size(12.dp),
                                        color = CustomTrafficWhite,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        text = "Добавить заправку",
                                        modifier = Modifier.align(Alignment.Center),
                                        color = CustomTrafficWhite,
                                        fontFamily = segoe_ui,
                                        fontSize = 12.sp
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
                                modifier = Modifier
                                    .size(24.dp),
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
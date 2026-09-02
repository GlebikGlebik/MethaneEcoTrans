package com.methane.eco.trans.presentation.regscreen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.draw.alpha
import androidx.navigation.NavController
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.methane.eco.trans.data.local.TokenStorage
import com.methane.eco.trans.data.repository.AuthRepositoryImpl
import com.methane.eco.trans.domain.usecase.AuthUseCase
import com.methane.eco.trans.domain.usecase.ValidationUseCase
import com.methane.eco.trans.presentation.viewmodel.RegViewModel
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHost
import com.methane.eco.trans.segoe_ui
import com.methane.eco.trans.segoe_ui_bold
import com.methane.eco.trans.theme.CustomTurquoiseBlue
import com.methane.eco.trans.theme.CustomTrafficWhite
import com.methane.eco.trans.theme.CustomCarpiBlue
import com.methane.eco.trans.theme.CustomEnterBarColor
import com.methane.eco.trans.theme.CustomGrey

@Composable
fun RegistrationScreen(navController: NavController, viewModel: RegViewModel = viewModel(
    factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            // Создаем TokenStorage
            val context = navController.context
            val tokenStorage = TokenStorage(context)

            // Создаем реальный репозиторий
            val repository = AuthRepositoryImpl(tokenStorage)

            // Создаем useCases
            val useCase = AuthUseCase(repository)
            val validationUseCase = ValidationUseCase()

            return RegViewModel(useCase, validationUseCase) as T
        }
    }
)) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when(event){
                is RegScreenEvent.NavigateToMainScreen -> { navController.navigate("MainScreen") {
                    popUpTo("RegistrationScreen") {inclusive}
                }}
                is RegScreenEvent.NavigateToEnterScreen -> { navController.navigate("EnterScreen")}
                is RegScreenEvent.ShowSnackbar -> { snackbarHostState.showSnackbar(event.message)}
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(CustomTurquoiseBlue)
    ) {
        // Получаем размеры внутреннего экрана, которые равняются половине экрана
        val boxWidth = this.maxWidth * 0.5f
        val boxHeight = this.maxHeight * 0.5f

        // Состояния для фокуса
        var isFocusedName by remember { mutableStateOf(false) }
        var isFocusedSurname by remember { mutableStateOf(false) }
        var isFocusedEmail by remember { mutableStateOf(false) }
        var isFocusedPassword by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .requiredSize(boxWidth, boxHeight)
                .align(Alignment.Center)
                .background(CustomTrafficWhite, shape = RoundedCornerShape(15.dp))
        ) {
            Text(
                text = "Регистрация",
                modifier = Modifier
                    .padding(top = boxHeight / 11 * 1)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontFamily = segoe_ui_bold,
                color = CustomCarpiBlue,
                fontSize = 24.sp
            )

            // Поле для ввода фамилии
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = boxHeight / 11 * 3 + 5.dp,
                        start = boxWidth / 11,
                        end = boxWidth / 11,
                        bottom = boxHeight / 11 * 7 + 5.dp
                    )
                    .background(CustomEnterBarColor, shape = RoundedCornerShape(10.dp))
            ) {
                // Плейсхолдер
                if (uiState.surname.isEmpty() && !isFocusedSurname) {
                    Text(
                        text = "фамилия",
                        modifier = Modifier
                            .alpha(0.5f)
                            .align(Alignment.Center)
                            .padding(
                                start = 5.dp,
                                end = 104.dp
                            ),
                        color = CustomGrey,
                        fontFamily = segoe_ui,
                        fontSize = 12.sp,
                    )
                }
                BasicTextField(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            start = 5.dp,
                            top = 10.dp,
                            end = 5.dp
                        )
                        .align(Alignment.Center)
                        .onFocusChanged { focusState -> isFocusedSurname = focusState.isFocused },
                    value = uiState.surname,
                    onValueChange = { newText ->
                        // Ограничиваем длину до 24 символов
                        if (newText.length <= 24) {
                            viewModel.onSurnameChanged(newText)
                        }
                    },
                    textStyle = TextStyle(
                        color = CustomGrey,
                        fontSize = 12.sp
                    ),
                    cursorBrush = Brush.verticalGradient(
                        colors = listOf(
                            CustomGrey.copy(alpha = 0.5f),
                            CustomGrey.copy(alpha = 0.5f)
                        ),
                        startY = 0f,
                        endY = 12f
                    )
                )
            }

            // Поле для ввода имени
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = boxHeight / 11 * 4 + 5.dp,
                        start = boxWidth / 11,
                        end = boxWidth / 11,
                        bottom = boxHeight / 11 * 6 + 5.dp
                    )
                    .background(CustomEnterBarColor, shape = RoundedCornerShape(10.dp))
            ) {
                // Плейсхолдер
                if (uiState.name.isEmpty() && !isFocusedName) {
                    Text(
                        text = "имя",
                        modifier = Modifier
                            .alpha(0.5f)
                            .align(Alignment.Center)
                            .padding(
                                start = 5.dp,
                                end = 135.dp
                            ),
                        color = CustomGrey,
                        fontFamily = segoe_ui,
                        fontSize = 12.sp,
                    )
                }
                BasicTextField(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            start = 5.dp,
                            top = 10.dp,
                            end = 5.dp
                        )
                        .align(Alignment.Center)
                        .onFocusChanged { focusState -> isFocusedName = focusState.isFocused },
                    value = uiState.name,
                    onValueChange = { newText ->
                        // Ограничиваем длину до 24 символов
                        if (newText.length <= 24) {
                            viewModel.onNameChanged(newText)
                        }
                    },
                    textStyle = TextStyle(
                        color = CustomGrey,
                        fontSize = 12.sp
                    ),
                    cursorBrush = Brush.verticalGradient(
                        colors = listOf(
                            CustomGrey.copy(alpha = 0.5f),
                            CustomGrey.copy(alpha = 0.5f)
                        ),
                        startY = 0f,
                        endY = 12f
                    )
                )
            }

            // Поле для ввода e-mail
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = boxHeight / 11 * 5 + 5.dp,
                        start = boxWidth / 11,
                        end = boxWidth / 11,
                        bottom = boxHeight / 11 * 5 + 5.dp
                    )
                    .background(CustomEnterBarColor, shape = RoundedCornerShape(10.dp))
            ) {
                // Плейсхолдер
                if (uiState.email.isEmpty() && !isFocusedEmail) {
                    Text(
                        text = "e-mail",
                        modifier = Modifier
                            .alpha(0.5f)
                            .align(Alignment.Center)
                            .padding(
                                start = 5.dp,
                                end = 121.dp
                            ),
                        color = CustomGrey,
                        fontFamily = segoe_ui,
                        fontSize = 12.sp,
                    )
                }
                BasicTextField(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            start = 5.dp,
                            top = 10.dp,
                            end = 5.dp
                        )
                        .align(Alignment.Center)
                        .onFocusChanged { focusState -> isFocusedEmail = focusState.isFocused },
                    value = uiState.email,
                    onValueChange = { newText ->
                        // Ограничиваем длину до 24 символов
                        if (newText.length <= 24) {
                            viewModel.onEmailChanged(newText)
                        }
                    },
                    textStyle = TextStyle(
                        color = CustomGrey,
                        fontSize = 12.sp
                    ),
                    cursorBrush = Brush.verticalGradient(
                        colors = listOf(
                            CustomGrey.copy(alpha = 0.5f),
                            CustomGrey.copy(alpha = 0.5f)
                        ),
                        startY = 0f,
                        endY = 12f
                    )
                )
            }

            // Поле для ввода пароля
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = boxHeight / 11 * 6 + 5.dp,
                        start = boxWidth / 11,
                        end = boxWidth / 11,
                        bottom = boxHeight / 11 * 4 + 5.dp
                    )
                    .background(CustomEnterBarColor, shape = RoundedCornerShape(10.dp)),
            ) {
                // Плейсхолдер
                if (uiState.password.isEmpty() && !isFocusedPassword) {
                    Text(
                        text = "пароль",
                        modifier = Modifier
                            .alpha(0.5f)
                            .align(Alignment.Center)
                            .padding(
                                start = 5.dp,
                                end = 115.dp
                            ),
                        color = CustomGrey,
                        fontFamily = segoe_ui,
                        fontSize = 12.sp,
                    )
                }
                // Сам ввод пароля
                BasicTextField(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            start = 5.dp,
                            top = 10.dp,
                            end = 5.dp
                        )
                        .align(Alignment.Center)
                        .onFocusChanged { focusState -> isFocusedPassword = focusState.isFocused },
                    value = uiState.password,
                    onValueChange = { newText ->
                        // Ограничиваем длину пароля до 24 символов
                        if (newText.length <= 24) {
                            viewModel.onPasswordChanged(newText)
                        }
                    },
                    textStyle = TextStyle(
                        color = CustomGrey,
                        fontSize = 12.sp
                    ),
                    cursorBrush = Brush.verticalGradient(
                        colors = listOf(
                            CustomGrey.copy(alpha = 0.5f),
                            CustomGrey.copy(alpha = 0.5f)
                        ),
                        startY = 0f,
                        endY = 12f
                    )
                )
            }

            // Кнопка регистрации
            Box(
                modifier = Modifier
                    .requiredSize(boxWidth, boxHeight)
                    .padding(
                        top = boxHeight / 11 * 8 + 5.dp,
                        start = boxWidth / 11,
                        end = boxWidth / 11,
                        bottom = boxHeight / 11 * 2 + 5.dp
                    )
                    .background(CustomCarpiBlue, shape = RoundedCornerShape(10.dp))
                    .clickable {
                        if (!uiState.isLoading) {
                            viewModel.onRegisterClick()
                        }
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
                        text = "Регистрация",
                        modifier = Modifier.align(Alignment.Center),
                        color = CustomTrafficWhite,
                        fontFamily = segoe_ui,
                        fontSize = 16.sp
                    )
                }
            }

            // Кнопка Вход
            Box(
                modifier = Modifier
                    .requiredSize(boxWidth, boxHeight)
                    .padding(
                        top = boxHeight / 11 * 9 + 5.dp,
                        start = boxWidth / 11,
                        end = boxWidth / 11,
                        bottom = boxHeight / 11 * 1 + 5.dp
                    )
                    .background(CustomCarpiBlue, shape = RoundedCornerShape(10.dp))
            ) {
                Text(
                    text = "Вход",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clickable { viewModel.onEnterClicked() },
                    color = CustomTrafficWhite,
                    fontFamily = segoe_ui,
                    fontSize = 16.sp
                )
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}
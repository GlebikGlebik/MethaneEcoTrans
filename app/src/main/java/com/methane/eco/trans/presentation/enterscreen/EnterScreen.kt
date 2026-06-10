package com.methane.eco.trans.presentation.enterscreen

import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.clickable
import androidx.navigation.NavController
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.methane.eco.trans.data.repository.MockAuthRepository
import com.methane.eco.trans.domain.usecase.AuthUseCase
import com.methane.eco.trans.presentation.viewmodel.EnterViewModel
import com.methane.eco.trans.segoe_ui
import com.methane.eco.trans.segoe_ui_bold
import com.methane.eco.trans.theme.CustomTurquoiseBlue
import com.methane.eco.trans.theme.CustomTrafficWhite
import com.methane.eco.trans.theme.CustomCarpiBlue
import com.methane.eco.trans.theme.CustomEnterBarColor
import com.methane.eco.trans.theme.CustomGrey


@Composable
fun EnterScreen(navController: NavController, viewModel: EnterViewModel = viewModel(
    factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val repository = MockAuthRepository() // <-- Здесь наша заглушка
            val useCase = AuthUseCase(repository)
            return EnterViewModel(useCase) as T
        }
    }
)) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is EnterScreenEvent.ShowSnackbar -> {snackbarHostState.showSnackbar(event.message)}
                is EnterScreenEvent.NavigateToMainScreen -> { navController.navigate("MainScreen") {
                    popUpTo("EnterScreen") {inclusive = true}
                } }
                is EnterScreenEvent.NavigateToRegistrationScreen -> { navController.navigate("RegistrationScreen") }
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

        //отслеживание состояний
        var isFocusedEmail by remember { mutableStateOf(false) }
        var isFocusedPassword by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .requiredSize(boxWidth, boxHeight)
                .align(Alignment.Center)
                .background(CustomTrafficWhite, shape = RoundedCornerShape(15.dp))
        ) {
            Text(
                text = "Вход",
                modifier = Modifier
                    .padding(top = boxHeight / 11 * 2)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontFamily = segoe_ui_bold,
                color = CustomCarpiBlue,
                fontSize = 24.sp
            )
            //  поле для ввода e-mail
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
                //плейсхолдер
                if(uiState.email.isEmpty() && !isFocusedEmail){
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
                        // Ограничиваем длину пароля до 14 символов
                        if (newText.length <= 24) {
                            viewModel.onEmailChanged(newText)
                        }
                    },
                    textStyle = TextStyle(
                        color = CustomGrey,
                        fontSize = 12.sp
                    ),
                    cursorBrush = Brush.verticalGradient(
                        colors = listOf(CustomGrey.copy(alpha = 0.5f), CustomGrey.copy(alpha = 0.5f)),
                        startY = 0f,
                        endY = 12f
                    )
                )
            }
            // поле дял ввода пароля
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = boxHeight / 11 * 5 + 5.dp,
                        start = boxWidth / 11,
                        end = boxWidth / 11,
                        bottom = boxHeight / 11 * 5 + 5.dp
                    )
                    .background(CustomEnterBarColor, shape = RoundedCornerShape(10.dp)),
            ){
                // плейсхолдер
                if (uiState.password.isEmpty() && !isFocusedPassword){
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
                // сам ввод пароля
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
                        // Ограничиваем длину пароля до 14 символов
                        if (newText.length <= 24) {
                            viewModel.onPasswordChanged(newText)
                        }
                    },
                    textStyle = TextStyle(
                        color = CustomGrey,
                        fontSize = 12.sp
                    ),
                    cursorBrush = Brush.verticalGradient(
                        colors = listOf(CustomGrey.copy(alpha = 0.5f), CustomGrey.copy(alpha = 0.5f)),
                        startY = 0f,
                        endY = 12f
                    )
                )
            }

            //Кнопка вход
            Box(
                modifier = Modifier
                    .requiredSize(boxWidth, boxHeight)
                    .padding(
                        top = boxHeight / 11 * 7 + 5.dp,
                        start = boxWidth / 11,
                        end = boxWidth / 11,
                        bottom = boxHeight / 11 * 3 + 5.dp

                    )
                    .background(CustomCarpiBlue, shape = RoundedCornerShape(10.dp))
                    .clickable{
                        if (!uiState.isLoading){
                            viewModel.onEnterClicked()
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
                        text = "Вход",
                        modifier = Modifier.align(Alignment.Center),
                        color = CustomTrafficWhite,
                        fontFamily = segoe_ui,
                        fontSize = 18.sp
                    )
                }
            }
            //Кнопка регистрация
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
            ) {
                Text(
                    text = "Регистрация",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clickable { viewModel.onRegistrationClicked() },
                    color = CustomTrafficWhite,
                    fontFamily = segoe_ui,
                    fontSize = 18.sp
                )

            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Preview(showBackground = true, name = "EnterScreenPreview")
@Composable
fun EnterScreenPreview() {
    // 1. Создаем тестовый NavController
    val navController = rememberNavController()

    // 2. Создаем зависимости вручную (без Hilt/Factory)
    val repository = MockAuthRepository()
    val useCase = AuthUseCase(repository)
    @Suppress("ViewModelConstructorInComposable")
    val viewModel = EnterViewModel(useCase)

    // 3. Передаем их в экран
    EnterScreen(
        navController = navController,
        viewModel = viewModel
    )
}

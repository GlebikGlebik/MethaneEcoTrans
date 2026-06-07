package com.methane.eco.trans


import android.util.Patterns
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
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import kotlinx.coroutines.launch
import androidx.compose.material3.CircularProgressIndicator
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.methane.eco.trans.presentation.sealedclass.EnterScreenEvent
import com.methane.eco.trans.presentation.viewmodel.AuthViewModel
import com.methane.eco.trans.theme.CustomTurquoiseBlue
import com.methane.eco.trans.theme.CustomTrafficWhite
import com.methane.eco.trans.theme.CustomCarpiBlue
import com.methane.eco.trans.theme.CustomEnterBarColor
import com.methane.eco.trans.theme.CustomGrey

@Composable
fun EnterScreen(navController: NavController, viewModel: AuthViewModel = viewModel()) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is EnterScreenEvent.ShowSnackbar -> { /* показать снекбар */ }
                is EnterScreenEvent.NavigateToMainScreen -> { navController.navigate("MainScreen") }
                is EnterScreenEvent.NavigateToRegistrationScreen -> { navController.navigate("RegistrationScreen") }
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(CustomTurquoiseBlue)
    ) {

        //корутина, экземпляр furebaseAuthentification и состояние для снекбара (вспылвашек)
        val auth = Firebase.auth
        val snackbarHostState = remember { SnackbarHostState() }
        val coroutineScope = rememberCoroutineScope()

        // Получаем размеры внутреннего экрана, которые равняются половине экрана
        val boxWidth = this.maxWidth * 0.5f
        val boxHeight = this.maxHeight * 0.5f

        //отслеживание состояний
        var password by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var isFocusedEmail by remember { mutableStateOf(false) }
        var isFocusedPassword by remember { mutableStateOf(false) }
        var isLoading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        // Состояния ошибок
        var emailError by remember { mutableStateOf(false) }
        var emptyFieldsError by remember { mutableStateOf(false) }

        // добавляем функцию, проверяющую корректность email
        fun isEmailValid(email: String): Boolean {
            return Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }

        fun onEnterClick(){
            //проверяем ошибки
            emailError = !isEmailValid(email)
            emptyFieldsError = email.isEmpty() || password.isEmpty()

            if (!emailError && !emptyFieldsError){
                signInUser(
                    auth = auth,
                    email = email,
                    password = password,
                    navController = navController,
                    snackbarHostState = snackbarHostState,
                    coroutineScope = coroutineScope
                )
            } else {
                if (emailError && !emptyFieldsError) {
                    isLoading = false
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            "Неверный формат email",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
                if (!emailError && emptyFieldsError) {
                    isLoading = false
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            "Необходимо заполнить все поля",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            }
        }

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
                if(email.isEmpty() && !isFocusedEmail){
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
                    value = email,
                    onValueChange = { newText ->
                        // Ограничиваем длину пароля до 14 символов
                        if (newText.length <= 24) {
                            email = newText
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
                if (password.isEmpty() && !isFocusedPassword){
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
                    value = password,
                    onValueChange = { newText ->
                        // Ограничиваем длину пароля до 14 символов
                        if (newText.length <= 24) {
                            password = newText
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
                    .clickable(){
                        if (!isLoading){
                            isLoading = true
                            onEnterClick()
                            isLoading = false
                        }
                    }
            ) {
                if (isLoading) {
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
                        .clickable { navController.navigate("RegistrationScreen") },
                    color = CustomTrafficWhite,
                    fontFamily = segoe_ui,
                    fontSize = 18.sp
                )

            }
        }
        androidx.compose.material3.SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}
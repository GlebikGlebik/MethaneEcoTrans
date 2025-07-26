package com.methane.eco.trans

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.navigation.NavController
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.SnackbarDuration
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.launch
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import com.methane.eco.trans.theme.CustomTurquoiseBlue
import com.methane.eco.trans.theme.CustomTrafficWhite
import com.methane.eco.trans.theme.CustomCarpiBlue
import com.methane.eco.trans.theme.CustomDeepOrange
import com.methane.eco.trans.theme.CustomEnterBarColor
import com.methane.eco.trans.theme.CustomGrey

@Composable
fun MainScreen(navController: NavController){
    // отследиванием состояние высплывающего окна
    var showRefuelDialog by remember { mutableStateOf(false) }

    // штуки для уведомлений
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    //штуки для работы с firebase
    val auth = Firebase.auth
    val database = FirebaseDatabase.getInstance("https://met-project-fdcef-default-rtdb.europe-west1.firebasedatabase.app/")
    val user = auth.currentUser
    Log.d("MainScreen", "Current UID: ${user?.uid ?: "null"}")
    val vehiclesRef = database.getReference("users").child(user?.uid.toString()).child("vehicles")

    // Отслеживание состояний
    var date by remember { mutableStateOf("") }
    var volume by remember { mutableStateOf("") }
    var sum by remember { mutableStateOf("") }
    var userVehicles by remember { mutableStateOf<List<String>>(emptyList()) }
    var newVehicle by remember { mutableStateOf("") }
    var currentVehicle by remember { mutableStateOf("") }

    // Состояния для фокуса
    var isFocusedDate by remember { mutableStateOf(false) }
    var isFocusedVolume by remember { mutableStateOf(false) }
    var isFocusedSum by remember { mutableStateOf(false) }
    var isFocusedNewVehicle by remember { mutableStateOf(false) }

    suspend fun addUserHistory() {
        //проверки
        if (date.isBlank() || volume.isBlank() || sum.isBlank()) {
            snackbarHostState.showSnackbar("Необходимо заполнить все поля")
            return
        }
        if (currentVehicle == "") {
            snackbarHostState.showSnackbar("Необходимо указать транспортное средство")
            return
        }
        if (!isDateValid(date)) {
            snackbarHostState.showSnackbar("Некорректный формат даты")
            return
        }

        // основной блок отправки
        try {
            val userUid = user?.uid ?: run {
                snackbarHostState.showSnackbar("Ошибка: пользователь не авторизован")
                return
            }

            // Функция для преобразования даты в Firebase-совместимый формат
            fun formatDateForFirebase(inputDate: String): String {
                return inputDate.replace(".", ";")
            }

            val firebaseDate = formatDateForFirebase(date)

            // Создаем ссылку на нужное место в базе данных
            val historyRef = database.getReference("history")
                .child(userUid)
                .child(currentVehicle)
                .child(firebaseDate) // Используем преобразованную дату

            // Создаем объект с данными заправки
            val refuelData = hashMapOf(
                "volume" to volume.toDouble(),
                "sum" to sum.toDouble()
            )

            // Сохраняем данные
            historyRef.setValue(refuelData).await()

            // Очищаем поля после успешного сохранения
            date = ""
            volume = ""
            sum = ""
            currentVehicle = ""

            snackbarHostState.showSnackbar("Данные о заправке сохранены")
        } catch (e: Exception) {
            snackbarHostState.showSnackbar("Ошибка при сохранении: ${e.message}")
        }
    }

    suspend fun deleteUserVehicle() {
        // Проверки
        if (newVehicle.isBlank()) {
            snackbarHostState.showSnackbar("Необходимо указать транспортное средство")
            return
        }

        try {
            val userUid = user?.uid ?: run {
                snackbarHostState.showSnackbar("Ошибка: пользователь не авторизован")
                return
            }

            val userRef = database.getReference("users/$userUid")

            // Получаем текущий список авто
            val snapshot = userRef.child("vehicles").get().await()
            val currentVehicles = snapshot.children.mapNotNull { it.getValue(String::class.java) }

            // Проверяем, существует ли такой автомобиль
            if (!currentVehicles.contains(newVehicle)) {
                snackbarHostState.showSnackbar("Такой автомобиль не найден")
                return
            }

            // Сразу обновляем локальное состояние (для мгновенного отображения в UI)
            userVehicles = userVehicles.filter { it != newVehicle }

            // Удаляем автомобиль из базы данных
            userRef.child("vehicles").setValue(userVehicles).await()

            // Если удаляемый автомобиль был выбран - сбрасываем выбор
            if (currentVehicle == newVehicle) {
                currentVehicle = ""
            }

            // Очищаем поле ввода
            newVehicle = ""

            snackbarHostState.showSnackbar("Автомобиль удален")
        } catch (e: Exception) {
            // В случае ошибки - восстанавливаем локальное состояние
            userVehicles = database.getReference("users/${user?.uid}/vehicles")
                .get().await()
                .children.mapNotNull { it.getValue(String::class.java) }

            snackbarHostState.showSnackbar("Ошибка при удалении: ${e.message}")
        }
    }

    suspend fun addUserVehicle() {
        if (newVehicle.isBlank()) {
            snackbarHostState.showSnackbar("Необходимо указать транспортное средство")
            return
        }


        try {
            val userRef = database.getReference("users/${user?.uid}")

            // Получаем текущий список авто
            val snapshot = userRef.child("vehicles").get().await()
            val currentVehicles = snapshot.children.mapNotNull { it.getValue(String::class.java) }

            // Проверяем, нет ли уже такого авто
            if (currentVehicles.contains(newVehicle)) {
                snackbarHostState.showSnackbar("Этот автомобиль уже добавлен")
                return
            }

            //Добавляем новый автомобиль
            val updatedVehicles = currentVehicles.toMutableList().apply { add(newVehicle) }

            //Сохраняем обновленный список
            userRef.child("vehicles").setValue(updatedVehicles).await()

            newVehicle = ""
            snackbarHostState.showSnackbar("Автомобиль добавлен")
        } catch (e: Exception) {
            snackbarHostState.showSnackbar("Ошибка: ${e.message}")
        }
    }

    LaunchedEffect(Unit) {
        vehiclesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val vehicles = userVehicles.toMutableList() // Сохраняем текущие значения
                snapshot.children.forEach { child ->
                    child.getValue(String::class.java)?.let { newVehicle ->
                        if (!vehicles.contains(newVehicle)) { // Проверяем, нет ли уже такого авто
                            vehicles.add(newVehicle)
                        }
                    }
                }
                userVehicles = vehicles
            }

            override fun onCancelled(error: DatabaseError) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Ошибка загрузки данных: ${error.message}",
                        duration = SnackbarDuration.Short
                    )
                }
            }
        })
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(color = CustomTrafficWhite)
    ){
        // Получаем размеры всего доступного экрана
        val boxWidth = this.maxWidth
        val boxHeight = this.maxHeight

        //кнопка добавить заправку
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
                    .background(color = CustomCarpiBlue, shape =  RoundedCornerShape(15.dp))
                    .clickable{ showRefuelDialog = true }

            ){
                Text(
                    text = "Добавить заправку",
                    modifier = Modifier
                        .align(Alignment.Center),
                    color = CustomTrafficWhite,
                    fontFamily = segoe_ui,
                    fontSize = 16.sp
                )
            }
        }

        // Кастомный диалог
        if (showRefuelDialog) {
            Dialog(
                onDismissRequest = { showRefuelDialog = false },
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
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(
                                    start = 12.dp,
                                    end = 12.dp,
                                    top = 12.dp,
                                    bottom = dialogHeight / 21 * 10 + 12.dp
                                )
                                .background(color = CustomTrafficWhite)
                        ){
                            Box(
                                modifier = Modifier
                                    .border(1.dp, color = CustomDeepOrange, shape = RoundedCornerShape(15.dp))
                                    .background(color = CustomTrafficWhite)
                            ){
                                // поле для выбора авто
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(
                                            top = dialogHeight / 21 * 1,
                                            start = dialogWidth / 11 * 3,
                                            end = dialogWidth / 11 * 3,
                                            bottom = dialogHeight / 21 * 9
                                        )
                                ){
                                    var expanded by remember { mutableStateOf(false) }
                                    val focusManager = LocalFocusManager.current

                                    Box(
                                        modifier = Modifier
                                            .background(CustomTurquoiseBlue, shape = RoundedCornerShape(15.dp))
                                            .requiredSize(dialogWidth / 11 * 5, dialogHeight / 21 * 1)
                                            .clickable{
                                                focusManager.clearFocus()
                                                expanded = true
                                            }
                                    ) {
                                        Text(
                                            text = currentVehicle.ifEmpty { "Номер авто ≡" },
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
                                            if (userVehicles.isEmpty()) {
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
                                                userVehicles.forEach { vehicle ->
                                                    DropdownMenuItem(
                                                        text = {
                                                            Text(
                                                                vehicle,
                                                                color = CustomTrafficWhite
                                                            )
                                                        },
                                                        onClick = {
                                                            currentVehicle = vehicle
                                                            expanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                // поле для ввода даты
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(
                                            top = dialogHeight / 21 * 3,
                                            start = dialogWidth / 11 * 1,
                                            end = dialogWidth / 11 * 1,
                                            bottom = dialogHeight / 21 * 7
                                        )
                                ){
                                    Box(
                                        modifier = Modifier
                                            .requiredSize(dialogWidth / 11 * 9, dialogHeight/21 * 1)
                                            .background(CustomEnterBarColor, shape = RoundedCornerShape(15.dp))
                                    ){
                                            //плейсхолдер
                                            BasicTextField(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .align(Alignment.Center)
                                                    .onFocusChanged { focusState -> isFocusedDate = focusState.isFocused },
                                                value = date,
                                                onValueChange = { newText ->
                                                    // Ограничиваем длину даты пароля до 10 символов
                                                    if (newText.length <= 10) {
                                                        date = newText
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
                                                        contentAlignment = Alignment.Center // Центрируем содержимое
                                                    ) {
                                                        if (date.isEmpty() && !isFocusedDate) {
                                                            Text(
                                                                text = "дата: xx.xx.xxxx",
                                                                modifier = Modifier.alpha(0.5f),
                                                                color = CustomGrey,
                                                                fontFamily = segoe_ui,
                                                                fontSize = 12.sp,
                                                                textAlign = TextAlign.Center // Центрируем плейсхолдер
                                                            )
                                                        }
                                                        innerTextField()
                                                    }
                                                }
                                            )
                                    }
                                }
                                // поле для ввода объема
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(
                                            top = dialogHeight / 21 * 5,
                                            start = dialogWidth / 11 * 1,
                                            end = dialogWidth / 11 * 1,
                                            bottom = dialogHeight / 21 * 5
                                        )
                                ){
                                    Box(
                                        modifier = Modifier
                                            .requiredSize(dialogWidth / 11 * 9, dialogHeight/21 * 1)
                                            .background(CustomEnterBarColor, shape = RoundedCornerShape(15.dp))
                                    ){
                                        //плейсхолдер
                                        BasicTextField(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .align(Alignment.Center)
                                                .onFocusChanged { focusState -> isFocusedVolume = focusState.isFocused },
                                            value = volume,
                                            onValueChange = { newText ->
                                                // Ограничиваем длину объема пароля до 10 символов
                                                if (newText.length <= 10) {
                                                    volume = newText
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
                                                    contentAlignment = Alignment.Center // Центрируем содержимое
                                                ) {
                                                    if (volume.isEmpty() && !isFocusedVolume) {
                                                        Text(
                                                            text = "объем",
                                                            modifier = Modifier.alpha(0.5f),
                                                            color = CustomGrey,
                                                            fontFamily = segoe_ui,
                                                            fontSize = 12.sp,
                                                            textAlign = TextAlign.Center // Центрируем плейсхолдер
                                                        )
                                                    }
                                                    innerTextField()
                                                }
                                            }
                                        )
                                    }
                                }
                                // поле для ввода суммы
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(
                                            top = dialogHeight / 21 * 7,
                                            start = dialogWidth / 11 * 1,
                                            end = dialogWidth / 11 * 1,
                                            bottom = dialogHeight / 21 * 3
                                        )
                                ){
                                    Box(
                                        modifier = Modifier
                                            .requiredSize(dialogWidth / 11 * 9, dialogHeight/21 * 1)
                                            .background(CustomEnterBarColor, shape = RoundedCornerShape(15.dp))
                                    ){
                                        //плейсхолдер
                                        BasicTextField(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .align(Alignment.Center)
                                                .onFocusChanged { focusState -> isFocusedSum = focusState.isFocused },
                                            value = sum,
                                            onValueChange = { newText ->
                                                // Ограничиваем длину объема пароля до 10 символов
                                                if (newText.length <= 10) {
                                                    sum = newText
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
                                                    contentAlignment = Alignment.Center // Центрируем содержимое
                                                ) {
                                                    if (sum.isEmpty() && !isFocusedSum) {
                                                        Text(
                                                            text = "сумма",
                                                            modifier = Modifier.alpha(0.5f),
                                                            color = CustomGrey,
                                                            fontFamily = segoe_ui,
                                                            fontSize = 12.sp,
                                                            textAlign = TextAlign.Center // Центрируем плейсхолдер
                                                        )
                                                    }
                                                    innerTextField()
                                                }
                                            }
                                        )
                                    }
                                }
                                // кнопка добавить заправку
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(
                                            top = dialogHeight / 21 * 9 - 4.dp,
                                            start = dialogWidth / 11 * 1,
                                            end = dialogWidth / 11 * 1,
                                            bottom = dialogHeight / 21 * 1 + 4.dp
                                        )
                                ){
                                    Box(
                                        modifier = Modifier
                                            .requiredSize(dialogWidth / 11 * 9, dialogHeight/21 * 1)
                                            .background(CustomCarpiBlue, shape = RoundedCornerShape(15.dp))
                                            .clickable{
                                                coroutineScope.launch {
                                                    addUserHistory()
                                                }
                                            }
                                    ){
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
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(
                                    start = 12.dp,
                                    end = 12.dp,
                                    top = dialogHeight/ 21 * 12,
                                    bottom = 12.dp
                                )
                        ){
                            Box(
                                modifier = Modifier
                                    .size(dialogWidth / 11 * 11, dialogHeight / 21 * 9)
                                    .border(1.dp, color = CustomDeepOrange, shape = RoundedCornerShape(15.dp))

                            ){
                                // кнопка добавить автомобиль
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(
                                            top = dialogHeight / 21 * 1,
                                            start = dialogWidth / 11 * 7,
                                            end = dialogWidth / 11 * 3,
                                            bottom = dialogHeight / 21 * 7
                                        )
                                ){
                                    Box(
                                        modifier = Modifier
                                            .requiredSize(dialogWidth / 11 * 1 + 2.dp, dialogHeight/21 * 1)
                                            .background(CustomCarpiBlue, shape = RoundedCornerShape(15.dp))
                                            .clickable{
                                                if (!newVehicle.isBlank()){
                                                    coroutineScope.launch {
                                                        addUserVehicle()
                                                    }
                                                } else {
                                                    coroutineScope.launch {
                                                        snackbarHostState.showSnackbar(
                                                            "Необходимо указать транспортное средство",
                                                            duration = SnackbarDuration.Short
                                                        )
                                                    }
                                                }
                                            }
                                    ){
                                        Text(
                                            text = "＋",
                                            modifier = Modifier.align(Alignment.Center),
                                            color = CustomTrafficWhite,
                                            fontFamily = segoe_ui,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                                // кнопка удалить автомобиль
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(
                                            top = dialogHeight / 21 * 1,
                                            start = dialogWidth / 11 * 9 - 3.dp,
                                            end = dialogWidth / 11 * 1 + 3.dp,
                                            bottom = dialogHeight / 21 * 7
                                        )
                                ){
                                    Box(
                                        modifier = Modifier
                                            .requiredSize(dialogWidth / 11 * 1 + 2.dp, dialogHeight/21 * 1)
                                            .background(CustomCarpiBlue, shape = RoundedCornerShape(15.dp))
                                            .clickable{
                                                if (!newVehicle.isBlank()){
                                                    coroutineScope.launch {
                                                        deleteUserVehicle()
                                                    }
                                                } else {
                                                    coroutineScope.launch {
                                                        snackbarHostState.showSnackbar(
                                                            "Необходимо указать транспортное средство",
                                                            duration = SnackbarDuration.Short
                                                        )
                                                    }
                                                }
                                            }
                                    ){
                                        Icon(
                                            painter = painterResource(R.drawable.vector_cross), // Ваш вектор
                                            contentDescription = "Удалить",
                                            modifier = Modifier
                                                .size(10.dp)
                                                .align(Alignment.Center),
                                            tint = CustomTrafficWhite
                                        )
                                    }
                                }
                                // поле для записи нового авто
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(
                                            top = dialogHeight / 21 * 1,
                                            start = dialogWidth / 11 * 1,
                                            end = dialogWidth / 11 * 5,
                                            bottom = dialogHeight / 21 * 7
                                        )
                                ){
                                    Box(
                                        modifier = Modifier
                                            .requiredSize(dialogWidth / 11 * 5, dialogHeight/21 * 1)
                                            .background(CustomEnterBarColor, shape = RoundedCornerShape(15.dp))
                                    ){
                                        //плейсхолдер
                                        BasicTextField(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .align(Alignment.Center)
                                                .onFocusChanged { focusState -> isFocusedNewVehicle = focusState.isFocused },
                                            value = newVehicle,
                                            onValueChange = { newText ->
                                                // Ограничиваем длину объема пароля до 10 символов
                                                if (newText.length <= 10) {
                                                    newVehicle = newText
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
                                                    contentAlignment = Alignment.Center // Центрируем содержимое
                                                ) {
                                                    if (newVehicle.isEmpty() && !isFocusedNewVehicle) {
                                                        Text(
                                                            text = "автомобиль",
                                                            modifier = Modifier.alpha(0.5f),
                                                            color = CustomGrey,
                                                            fontFamily = segoe_ui,
                                                            fontSize = 10.sp,
                                                            textAlign = TextAlign.Center // Центрируем плейсхолдер
                                                        )
                                                    }
                                                    innerTextField()
                                                }
                                            }
                                        )
                                    }
                                }
                                // перечень автомобилей
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(
                                            top = dialogHeight / 21 * 3,
                                            start = dialogWidth / 11 * 1,
                                            end = dialogWidth / 11 * 1,
                                            bottom = dialogHeight / 21 * 1
                                        )
                                ){
                                    Box(
                                        modifier = Modifier
                                            .requiredSize(dialogWidth/ 11 * 9, dialogHeight/21 * 5)
                                    ){
                                        if (userVehicles.isEmpty()){
                                            Text(
                                                text = "Автомобили еще не добавлены",
                                                modifier = Modifier.align(Alignment.Center),
                                                color = CustomGrey,
                                                fontFamily = segoe_ui,
                                                fontSize = 10.sp
                                            )
                                        } else {
                                            LazyColumn(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(CustomTrafficWhite)
                                            ) {
                                                items(userVehicles) { vehicle ->
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(vertical = 4.dp)
                                                    ) {
                                                        Text(
                                                            text = vehicle,
                                                            modifier = Modifier
                                                                .padding(horizontal = 8.dp, vertical = 6.dp),
                                                            color = CustomGrey,
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
                        }
                    }
                }
            }
        }

        // поле с акциями
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = boxWidth / 10,
                    end = boxWidth / 10,
                    top = boxHeight/ 18 * 10,
                    bottom = boxHeight / 18 + 12.dp
                )
        ){
            Box(
                modifier = Modifier
                    .size(boxWidth / 10 * 8, boxHeight / 18 * 6 )
                    .border(1.dp, CustomDeepOrange, shape = RoundedCornerShape(15.dp))
                    .background(color = CustomTrafficWhite, shape = RoundedCornerShape(15.dp))
            ){

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
        ){
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        horizontal = boxWidth / 10,
                        vertical = 12.dp
                    )
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(15.dp),
                    border = BorderStroke(1.dp, CustomDeepOrange),
                    color = CustomTrafficWhite
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp), // Отступы по бокам
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
                                    .size(24.dp), // Увеличиваем общий размер
                                contentAlignment = Alignment.Center
                            ){
                                Image(
                                    painter = painterResource(id = iconRes),
                                    contentDescription = "${description}Icon",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable{
                                            if (description == "profile"){
                                                navController.navigate("ProfileScreen")
                                            }
                                            if (description == "history"){
                                                navController.navigate("HistoryScreen")
                                            }
                                        }
                                )
                            }
                        }
                    }
                }
            }
        }
        androidx.compose.material3.SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.TopCenter) // Или TopCenter, если нужно сверху
        )
    }
}
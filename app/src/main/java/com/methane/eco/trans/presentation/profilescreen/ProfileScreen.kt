package com.methane.eco.trans.presentation.profilescreen

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.SnackbarDuration
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.methane.eco.trans.theme.CustomGrey
import com.methane.eco.trans.theme.CustomTrafficWhite
import com.methane.eco.trans.theme.CustomTurquoiseBlue
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.methane.eco.trans.R
import com.methane.eco.trans.segoe_ui
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
fun ProfileScreen(navController: NavController){
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(color = CustomTrafficWhite)
    ){
        // Получаем размеры всего доступного экрана
        val boxWidth = this.maxWidth
        val boxHeight = this.maxHeight

        //взаимодействие с realtime database
        val database = FirebaseDatabase.getInstance("https://met-project-fdcef-default-rtdb.europe-west1.firebasedatabase.app/")
        val ref = database.getReference("users")

        // штуки для уведомлений
        val snackbarHostState = remember { SnackbarHostState() }
        val coroutineScope = rememberCoroutineScope()

        // Состояние для хранения последних заправок
        val latestRefills = remember { mutableStateOf<List<Triple<String, Double, Double>>>(emptyList()) }

        // экземпляры firebase
        val auth = Firebase.auth
        val user = auth.currentUser
        val userUid: String? = user?.uid
        Log.d("userUID", "Current UID: ${user?.uid ?: "null"}")
        val historyRef: DatabaseReference = database.reference.child("history")

        // данные
        var userHistory by remember {
            mutableStateOf<Map<String, Map<String, Map<String, Double>>>>(emptyMap())
        }
        var userName by remember { mutableStateOf("") }
        var userSurname by remember { mutableStateOf("") }

        // переменные ранспорта
        var currentVehicle by remember { mutableStateOf("") }
        var userVehicles by remember { mutableStateOf<List<String>>(emptyList()) }
        val vehiclesRef = database.getReference("users").child(user?.uid.toString()).child("vehicles")
        val userNameRef = database.getReference("users").child(user?.uid.toString()).child("name")
        val userSurnameRef = database.getReference("users").child(user?.uid.toString()).child("surname")


        // Добавляем состояния для хранения сумм
        var totalFuel by remember { mutableStateOf(0.0) }
        var totalSum by remember { mutableStateOf(0.0) }

        // Состояния для хранения сравнения месяцев
        var currentMonthStats by remember { mutableStateOf(Pair(0.0, 0.0)) } // (volume, sum)
        var previousMonthStats by remember { mutableStateOf(Pair(0.0, 0.0)) }
        var monthlyComparison by remember { mutableStateOf(Pair(0.0, 0.0)) } // (volumeDiff, sumDiff)

        // Получение данных пользователя
        LaunchedEffect(Unit) {
            userNameRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    userName = snapshot.getValue(String::class.java) ?: ""
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("ProfileScreen", "Error loading name: ${error.message}")
                }
            })

            userSurnameRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    userSurname = snapshot.getValue(String::class.java) ?: ""
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("ProfileScreen", "Error loading surname: ${error.message}")
                }
            })
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

        LaunchedEffect(userUid) {
            userUid?.let { uid ->
                historyRef.child(uid).addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val historyMap = mutableMapOf<String, Map<String, Map<String, Double>>>()

                        snapshot.children.forEach { vehicleSnapshot ->
                            val vehicleName = vehicleSnapshot.key ?: return@forEach
                            val datesMap = mutableMapOf<String, Map<String, Double>>()

                            vehicleSnapshot.children.forEach { dateSnapshot ->
                                val date = dateSnapshot.key ?: return@forEach
                                val volume = dateSnapshot.child("volume").getValue(Double::class.java) ?: 0.0
                                val sum = dateSnapshot.child("sum").getValue(Double::class.java) ?: 0.0

                                datesMap[date] = mapOf(
                                    "volume" to volume,
                                    "sum" to sum
                                )
                            }

                            historyMap[vehicleName] = datesMap
                        }
                        userHistory = historyMap
                    }

                    override fun onCancelled(error: DatabaseError) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Ошибка загрузки истории: ${error.message}",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                })
            }
        }

        // Получаем данные из userHistory
        LaunchedEffect(userHistory, currentVehicle) {
            val tempList = mutableListOf<Triple<String, Double, Double>>()

            // Фильтруем записи только для выбранного автомобиля
            userHistory[currentVehicle]?.forEach { (date, data) ->
                val volume = data["volume"] ?: 0.0
                val sum = data["sum"] ?: 0.0
                tempList.add(Triple(date, volume, sum))
            }

            // Сортируем по дате (новые сначала) и берем 6 последние записи
            latestRefills.value = tempList
                .sortedByDescending { it.first }
                .take(6)
        }

        // Эффект для подсчета сумм при изменении истории или выбранного автомобиля
        LaunchedEffect(userHistory, currentVehicle) {
            var fuel = 0.0
            var sum = 0.0

            // Проверяем, что автомобиль выбран
            if (currentVehicle.isNotBlank()) {
                userHistory[currentVehicle]?.forEach { (_, data) ->
                    fuel += data["volume"] ?: 0.0
                    sum += data["sum"] ?: 0.0
                }
            }

            totalFuel = fuel
            totalSum = sum

            // Логируем для отладки
            Log.d("ProfileScreen", "Total for $currentVehicle: fuel=$fuel, sum=$sum")
        }

        LaunchedEffect(userHistory, currentVehicle) {
            val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1 // +1 т.к. месяцы с 0
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val prevMonth = if (currentMonth == 1) 12 else currentMonth - 1
            val prevYear = if (currentMonth == 1) currentYear - 1 else currentYear

            var currentVol = 0.0
            var currentSm = 0.0
            var prevVol = 0.0
            var prevSm = 0.0

            if (currentVehicle.isNotBlank()) {
                userHistory[currentVehicle]?.forEach { (dateStr, data) ->
                    val parts = dateStr.split(";")
                    if (parts.size == 3) {
                        val day = parts[0].toIntOrNull() ?: 0
                        val month = parts[1].toIntOrNull() ?: 0
                        val year = parts[2].toIntOrNull() ?: 0

                        when {
                            // Текущий месяц
                            month == currentMonth && year == currentYear -> {
                                currentVol += data["volume"] ?: 0.0
                                currentSm += data["sum"] ?: 0.0
                            }
                            // Прошлый месяц
                            month == prevMonth && year == prevYear -> {
                                prevVol += data["volume"] ?: 0.0
                                prevSm += data["sum"] ?: 0.0
                            }
                        }
                    }
                }
            }

            currentMonthStats = Pair(currentVol, currentSm)
            previousMonthStats = Pair(prevVol, prevSm)
            monthlyComparison = Pair(currentVol - prevVol, currentSm - prevSm)
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = boxHeight/19 * 1,
                    bottom = boxHeight/19 * 16,
                    start = boxWidth / 10 * 3 + 12.dp,
                    end = boxWidth / 10 * 3
                )
        ){
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ){
                Text(
                    text = "$userSurname $userName",
                    color = CustomGrey,
                    fontFamily = segoe_ui,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = boxHeight/19 * 1,
                    bottom = boxHeight/19 * 15 + 12.dp,
                    start = boxWidth / 10 - 12.dp,
                    end = boxWidth / 10 * 7 - 12.dp
                )
        ){
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(2.dp, CustomTurquoiseBlue, RoundedCornerShape(15.dp))
            ){
                Image(
                    painter = painterResource(R.drawable.vector_profile),
                    contentDescription = "profileIcon",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center)
                )
            }

        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = boxHeight/19 * 9,
                    bottom = boxHeight/19 * 9 + 12.dp,
                    start = boxWidth / 10,
                    end = boxWidth / 10
                )
        ){
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(1.dp, CustomTurquoiseBlue, RoundedCornerShape(15.dp))
            ) {
                Text(
                    text = "За все время вы потратили ${"%.1f".format(totalFuel)} л. и ${"%.2f".format(totalSum)} р.",
                    color = CustomGrey,
                    fontFamily = segoe_ui,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
        // поле с предварительной историей
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = boxHeight/19 * 10,
                    bottom = boxHeight/19 * 2,
                    start = boxWidth / 10,
                    end = boxWidth / 10
                )
        ){
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = CustomTurquoiseBlue, shape = RoundedCornerShape(15.dp))
            ){
                // поле "История"
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = 12.dp,
                            start = boxWidth / 10,
                            bottom = boxHeight / 18 * 6 - 12.dp,
                            end = boxWidth / 10 * 4 - 6.dp
                        )
                ){
                    Box(
                        modifier = Modifier
                            .size(boxWidth / 10 * 3, boxHeight / 18)
                            .border(1.dp, CustomTrafficWhite, shape = RoundedCornerShape(15.dp))
                    ){
                        Text(
                            text = "История",
                            modifier = Modifier
                                .align(Alignment.Center),
                            color = CustomTrafficWhite,
                            fontFamily = segoe_ui,
                            fontSize = 16.sp
                        )
                    }
                }
                // "поле выбор авто"
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = 12.dp,
                            start = boxWidth / 10 * 4 + 6.dp,
                            bottom = boxHeight / 18 * 6 - 12.dp,
                            end = boxWidth / 10
                        )
                ){
                    var expanded by remember { mutableStateOf(false) }
                    val focusManager = LocalFocusManager.current

                    Box(
                        modifier = Modifier
                            .size(boxWidth / 10 * 4, boxHeight / 18 * 1)
                            .background(color = CustomTurquoiseBlue, shape = RoundedCornerShape(15.dp))
                            .border(1.dp, CustomTrafficWhite, RoundedCornerShape(15.dp))
                            .clickable {
                                focusManager.clearFocus()
                                expanded = true
                            }
                    ) {
                        Text(
                            text = currentVehicle.ifEmpty { "Номер авто ≡" },
                            modifier = Modifier.align(Alignment.Center),
                            color = CustomTrafficWhite,
                            fontFamily = segoe_ui,
                            fontSize = 16.sp
                        )
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier
                                .width(boxWidth / 10 * 4)
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

                // поле с самой историей
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = boxHeight / 18,
                            start = boxWidth / 10 ,
                            bottom = boxHeight / 18,
                            end = boxWidth / 10
                        )
                ){
                    Box(
                        modifier = Modifier
                            .size(boxWidth / 10 * 6, boxHeight / 18 * 5)
                            .background(color = CustomTrafficWhite, shape = RoundedCornerShape(15.dp))
                    ){
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(
                                    top = 12.dp,
                                    start = 12.dp,
                                    end = 12.dp,
                                    bottom = 12.dp
                                )
                        ){
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                if (currentVehicle.isBlank()) {
                                    Text(
                                        text = "Выберите автомобиль",
                                        color = CustomGrey,
                                        fontFamily = segoe_ui,
                                        fontSize = 16.sp,
                                        modifier = Modifier.fillMaxSize(),
                                        textAlign = TextAlign.Center
                                    )
                                } else {
                                    // Отображаем последние 6 заправок
                                    latestRefills.value.forEach { (date, volume, sum) ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = date.replace(";", "."),
                                                color = CustomGrey,
                                                fontFamily = segoe_ui,
                                                fontSize = 12.sp
                                            )
                                            Text(
                                                text = "%.1f л".format(volume),
                                                color = CustomGrey,
                                                fontFamily = segoe_ui,
                                                fontSize = 12.sp
                                            )
                                            Text(
                                                text = "%.2f ₽".format(sum),
                                                color = CustomGrey,
                                                fontFamily = segoe_ui,
                                                fontSize = 12.sp
                                            )
                                        }
                                        Divider(color = CustomGrey.copy(alpha = 0.3f), thickness = 1.dp)
                                    }
                                }

                            }

                        }
                    }
                }
                // поле загрузить еще
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = boxHeight / 18 * 6 - 12.dp,
                            start = boxWidth / 10 ,
                            bottom = 12.dp,
                            end = boxWidth / 10
                        )
                ){
                    Text(
                        text = "Загрузить еще",
                        modifier = Modifier
                            .clickable{navController.navigate("HistoryScreen")}
                            .align(Alignment.Center),
                        color = CustomTrafficWhite,
                        fontFamily = segoe_ui,
                        fontSize = 12.sp
                    )
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
                    border = BorderStroke(2.dp, CustomTurquoiseBlue),
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
                            R.drawable.vector_home to "main"
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
                                            if (description == "main"){
                                                navController.navigate("MainScreen")
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
    }
}

@Composable
@Preview(showBackground = true)
fun PreviewProfileScreen() {
    val navController = rememberNavController()
    ProfileScreen(navController)
}
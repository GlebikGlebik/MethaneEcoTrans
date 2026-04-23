package com.methane.eco.trans

import androidx.compose.runtime.Composable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.navigation.NavController
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.SnackbarDuration
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalFocusManager
import kotlinx.coroutines.launch
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.database.FirebaseDatabase
import com.methane.eco.trans.theme.CustomTurquoiseBlue
import com.methane.eco.trans.theme.CustomTrafficWhite
import com.methane.eco.trans.theme.CustomDeepOrange
import com.methane.eco.trans.theme.CustomGrey
import com.google.firebase.database.DatabaseReference
import kotlin.text.ifEmpty

@Composable
fun HistoryScreen(navController: NavController) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(color = CustomTurquoiseBlue)
    ) {
        // Получаем размеры всего доступного экрана
        val boxWidth = this.maxWidth
        val boxHeight = this.maxHeight

        // экземпляры firebase
        val auth = Firebase.auth
        val database = FirebaseDatabase.getInstance("https://met-project-fdcef-default-rtdb.europe-west1.firebasedatabase.app/")
        val user = auth.currentUser
        val userUid: String? = user?.uid
        Log.d("userUID", "Current UID: ${user?.uid ?: "null"}")
        val historyRef: DatabaseReference = database.reference.child("history")
        // данные
        var userHistory by remember {
            mutableStateOf<Map<String, Map<String, Map<String, Double>>>>(emptyMap())
        }

        // штуки для уведомлений
        val snackbarHostState = remember { SnackbarHostState() }
        val coroutineScope = rememberCoroutineScope()

        // переменные ранспорта
        var currentVehicle by remember { mutableStateOf("") }
        var userVehicles by remember { mutableStateOf<List<String>>(emptyList()) }
        val vehiclesRef = database.getReference("users").child(user?.uid.toString()).child("vehicles")

        // флаги
        var currentMonth by remember { mutableStateOf<Int?>(null) }

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
        //поле "История"
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
                    modifier = Modifier
                        .align(Alignment.Center),
                    color = CustomTrafficWhite,
                    fontFamily = segoe_ui,
                    fontSize = 16.sp
                )
            }
        }
        // поле выбора номера авто
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
        // внешнее поле с прокруткой
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = boxWidth / 10 - 12.dp,
                    end = boxWidth / 10 * 1 - 12.dp,
                    top = boxHeight / 18 * 2 + 12.dp,
                    bottom = boxHeight / 18 * 3 - 12.dp
                )
        ){
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = CustomTrafficWhite, shape = RoundedCornerShape(15.dp))
                    .border(1.dp, CustomDeepOrange, shape = RoundedCornerShape(15.dp)),
            ) {
                // раскидываем историю по группам-месяцам {05;2025=[(16;05;2025, {volume=12.8, sum=1256.0})]}
                val groupedHistory = remember(userHistory, currentVehicle) {
                    if (currentVehicle.isEmpty()) {
                        return@remember emptyMap<String, List<Pair<String, Map<String, Double>>>>()
                    }
                    userHistory[currentVehicle]?.let { vehicleHistory ->
                        vehicleHistory.toList()
                            .sortedByDescending { (date, _) ->
                                try {
                                    val parts = date.split(";")
                                    val day = parts[0].toInt()
                                    val month = parts[1].toInt()
                                    val year = parts[2].toInt()
                                    year * 10000 + month * 100 + day
                                } catch (e: Exception) {
                                    0
                                    Log.e("HistoryScreen", "Ошибка сортировки:$e")
                                }
                            }
                            .groupBy { (date, _) ->
                                try {
                                    val parts = date.split(";")
                                    "${parts[1]};${parts[2]}" // "MM;YYYY" как ключ группировки
                                } catch (e: Exception) {
                                    "0;0"
                                }
                            }
                    } ?: emptyMap()
                }
                Log.d("HistoryScreen", "userHistory после сортировки:$userHistory")
                Log.d("HistoryScreen", "groupedHistory после сортировки:$groupedHistory")
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (currentVehicle.isBlank()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                            ){
                                Text(
                                    text = "Выберите автомобиль для просмотра истории",
                                    color = CustomGrey,
                                    fontFamily = segoe_ui,
                                    fontSize = 24.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.Center)
                                )
                            }

                        }
                    } else if (groupedHistory.isEmpty()) {
                        item {
                            Text(
                                text = "Нет данных о заправках для выбранного автомобиля",
                                color = CustomGrey,
                                fontFamily = segoe_ui,
                                fontSize = 24.sp,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .align(Alignment.Center)
                            )
                        }
                    } else {
                        groupedHistory.forEach { (monthYear, records) ->
                            val (monthStr, yearStr) = monthYear.split(";")
                            val month = monthStr.toIntOrNull() ?: 0
                            val year = yearStr.toIntOrNull() ?: 0

                            item {
                                val monthName = when(month) {
                                    1 -> "Январь"
                                    2 -> "Февраль"
                                    3 -> "Март"
                                    4 -> "Апрель"
                                    5 -> "Май"
                                    6 -> "Июнь"
                                    7 -> "Июль"
                                    8 -> "Август"
                                    9 -> "Сентябрь"
                                    10 -> "Октябрь"
                                    11 -> "Ноябрь"
                                    12 -> "Декабрь"
                                    else -> "Неизвестный месяц"
                                }
                                Text(
                                    text = "$monthName $year",
                                    color = CustomGrey,
                                    fontFamily = segoe_ui,
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(vertical = 12.dp)
                                )
                            }

                            items(records) { (date, data) ->
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

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Text(
                                        text = "Объем: ${data["volume"] ?: 0.0} л",
                                        color = CustomGrey,
                                        fontFamily = segoe_ui,
                                        fontSize = 12.sp
                                    )

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Text(
                                        text = "Сумма: ${data["sum"] ?: 0.0} ₽",
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = boxWidth / 10 - 12.dp,
                    end = boxWidth / 10 * 1 - 12.dp,
                    top = boxHeight / 18 * 16 + 12.dp,
                    bottom = boxHeight / 18 * 1 - 12.dp
                )
        ){
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = CustomTrafficWhite, shape = RoundedCornerShape(15.dp))
            ){
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
                            R.drawable.vector_profile to "profile",
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
                                            if (description == "profile"){
                                                navController.navigate("ProfileScreen")
                                            }
                                            else if (description == "main"){
                                                navController.navigate("MainScreen")
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
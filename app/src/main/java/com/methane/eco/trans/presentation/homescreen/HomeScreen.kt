@file:Suppress("UNCHECKED_CAST")

package com.methane.eco.trans.presentation.homescreen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import com.methane.eco.trans.R
import com.methane.eco.trans.data.local.TokenStorage
import com.methane.eco.trans.data.repository.MainRepositoryImpl
import com.methane.eco.trans.domain.model.HomeActionItem
import com.methane.eco.trans.domain.model.NavBarItem
import com.methane.eco.trans.domain.usecase.AddRefuelingUseCase
import com.methane.eco.trans.domain.usecase.AddVehicleUseCase
import com.methane.eco.trans.domain.usecase.DeleteVehicleUseCase
import com.methane.eco.trans.domain.usecase.GetRefuelingHistoryUseCase
import com.methane.eco.trans.domain.usecase.GetVehiclesUseCase
import com.methane.eco.trans.presentation.viewmodel.HomeScreenViewModel
import com.methane.eco.trans.presentation.components.AppBottomNavBar
import com.methane.eco.trans.segoe_ui
import com.methane.eco.trans.segoe_ui_bold
import com.methane.eco.trans.theme.CustomCarpiBlue
import com.methane.eco.trans.theme.CustomDeepOrange
import com.methane.eco.trans.theme.CustomGrey
import com.methane.eco.trans.theme.CustomTrafficWhite
import com.methane.eco.trans.theme.CustomTurquoiseBlue

@Composable
fun MainScreen(
    navController: NavController,
    viewModel: HomeScreenViewModel = viewModel(
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

                return HomeScreenViewModel(
                    getVehiclesUseCase,
                    addVehicleUseCase,
                    deleteVehicleUseCase,
                    addRefuelingUseCase,
                    getRefuelingHistoryUseCase,
                    tokenStorage
                ) as T
            }
        }
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Обработка событий
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is HomeScreenEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is HomeScreenEvent.NavigateToHistoryScreen -> navController.navigate("HistoryScreen")
                is HomeScreenEvent.NavigateToProfileScreen -> navController.navigate("ProfileScreen")
                is HomeScreenEvent.NavigateToSettingsScreen -> navController.navigate("SettingsScreen")
                is HomeScreenEvent.NavigateToQrScreen -> navController.navigate("QrScreen")
                is HomeScreenEvent.NavigateToHomeScreen -> navController.navigate("MainScreen")
                is HomeScreenEvent.NavigateToMoreScreen -> navController.navigate("MoreScreen")
            }
        }
    }
    Scaffold(
        containerColor = CustomTrafficWhite,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            AppBottomNavBar(
                items = listOf(
                    NavBarItem(R.drawable.vector_home, "homeIcon", onClick = {}, true),
                    NavBarItem(R.drawable.vector_history, "historyIcon", onClick = viewModel::onHistoryClicked),
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
                HomeHeader(
                    userFullName = uiState.userFullName,
                    onSettingsClick = viewModel::onSettingsClicked
                )
            }

            item {
                Spacer(Modifier.height(8.dp))
                CardBlock(
                    cardNumber = uiState.discountCardID,
                    discountAmount = uiState.discountAmount
                )
            }

            item {
                Spacer(Modifier.height(8.dp))
                SectionTitle("Новости")
                HomeActionRow(items = uiState.newsItems, onItemClick = viewModel::onActionItemClicked)
            }

            item {
                Spacer(Modifier.height(8.dp))
                SectionTitle("Сервисы")
                HomeActionRow(items = uiState.serviceItems, onItemClick = viewModel::onActionItemClicked)
            }

            item {
                Spacer(Modifier.height(8.dp))
                SectionTitle("Забота о природе")
                EcoStatsBlock(summary = uiState.ecoStatsSummary)
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

// ============================== Шапка ==============================

@Composable
private fun HomeHeader(userFullName: String, onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // TODO: добавить реальный логотип в drawable (ic_logo)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(50))
                    .background(CustomCarpiBlue),
                contentAlignment = Alignment.Center
            ) {
                Text("сн", color = CustomTrafficWhite, fontFamily = segoe_ui_bold, fontSize = 14.sp)
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = userFullName.ifBlank { "Гость" },
                color = CustomGrey,
                fontFamily = segoe_ui,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
        IconButton(onClick = onSettingsClick) {
            Icon(
                painter = painterResource(id = R.drawable.vector_settings),
                contentDescription = "settingsIcon",
                tint = CustomTurquoiseBlue,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

//Дисконтная карта
@Composable
private fun CardBlock(cardNumber: String, discountAmount: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .border(2.5.dp, CustomTurquoiseBlue, RoundedCornerShape(20.dp))
            .background(CustomTrafficWhite)
            .padding(20.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("МЭТР", color = CustomCarpiBlue, fontFamily = segoe_ui_bold, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(Modifier.width(10.dp))
                Text(
                    "Карта постоянного клиента",
                    color = CustomGrey,
                    fontFamily = segoe_ui,
                    fontSize = 16.sp
                )
            }

            Spacer(Modifier.height(20.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                //TODO: QR должен был выровнен по левому краю
                Icon(
                    painter = painterResource(id = R.drawable.vector_qr),
                    contentDescription = "material_icon",
                    tint = CustomCarpiBlue,
                    modifier = Modifier.size(128.dp)
                )

                Spacer(Modifier.height(8.dp))

                Column {
                    Text(
                        "Номер карты",
                        color = CustomGrey,
                        fontFamily = segoe_ui,
                        fontSize = 11.sp
                    )
                    Text(
                        text = cardNumber.ifBlank {
                            "•••• ••••"
                        },
                        color = CustomCarpiBlue,
                        fontFamily = segoe_ui_bold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        "текущая скидка",
                        color = CustomGrey,
                        fontFamily = segoe_ui,
                        fontSize = 11.sp
                    )
                    Text(
                        "$discountAmount% скидка",
                        color = CustomDeepOrange,
                        fontFamily = segoe_ui_bold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

//Заголовки секций и карточки
@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = CustomTurquoiseBlue,
        fontFamily = segoe_ui_bold,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
    )
}

@Composable
private fun HomeActionRow(items: List<HomeActionItem>, onItemClick: (HomeActionItem) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(items = items, key = { it.id }) { item ->
            HomeActionCard(item = item, onClick = { onItemClick(item) })
        }
    }
}

@Composable
private fun HomeActionCard(item: HomeActionItem, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(224.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(2.5.dp, CustomTurquoiseBlue, RoundedCornerShape(16.dp))
            .background(CustomTrafficWhite)
            .padding(16.dp)
    ) {
        // TODO: когда появятся картинки — заменим на AsyncImage
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)){
            Box(
                modifier = Modifier
                    .size(45.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CustomTurquoiseBlue.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = item.icon ?: R.drawable.vector_news),
                    contentDescription = item.title,
                    tint = CustomCarpiBlue,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = item.title,
                color = CustomGrey,
                fontFamily = segoe_ui_bold,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                lineHeight = 18.sp,
                maxLines = 2,
                textAlign = TextAlign.Left
            )
        }
        Spacer(Modifier.height(14.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
                .clip(RoundedCornerShape(50))
                .background(CustomTurquoiseBlue)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Text("подробнее", color = CustomTrafficWhite, fontFamily = segoe_ui, fontSize = 12.sp)
        }
    }
}

// эко-статистика
@Composable
private fun EcoStatsBlock(summary: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .border(2.5.dp, CustomTurquoiseBlue, RoundedCornerShape(20.dp))
            .background(CustomTrafficWhite)
            .leafPatternBackground(leafColor = CustomTurquoiseBlue)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(CustomTurquoiseBlue),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.vector_eco),
                contentDescription = null,
                tint = CustomTrafficWhite,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        Text(
            text = summary,
            color = CustomCarpiBlue,
            fontFamily = segoe_ui_bold,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            lineHeight = 18.sp
        )
    }
}



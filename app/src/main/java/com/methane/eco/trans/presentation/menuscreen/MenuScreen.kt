@file:OptIn(ExperimentalMaterial3Api::class)
@file:Suppress("UNCHECKED_CAST")

package com.methane.eco.trans.presentation.menuscreen


import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.methane.eco.trans.R
import com.methane.eco.trans.data.local.TokenStorage
import com.methane.eco.trans.domain.model.NavBarItem
import com.methane.eco.trans.presentation.components.AppBottomNavBar
import com.methane.eco.trans.presentation.viewmodel.MenuScreenViewModel
import com.methane.eco.trans.domain.model.MenuButton
import com.methane.eco.trans.segoe_ui
import com.methane.eco.trans.segoe_ui_bold
import com.methane.eco.trans.theme.CustomEnterBarColor
import com.methane.eco.trans.theme.CustomErrorBarBackgroundColor
import com.methane.eco.trans.theme.CustomGrey
import com.methane.eco.trans.theme.CustomTrafficWhite
import com.methane.eco.trans.theme.CustomTurquoiseBlue
import kotlinx.coroutines.launch

private const val SUPPORT_PHONE_DISPLAY = "8 912 102 44 10"
private const val SUPPORT_PHONE_DIAL = "89121024410"

@Composable
fun MenuScreen(
    navController: NavController,
    viewModel: MenuScreenViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val context = navController.context
                val tokenStorage = TokenStorage(navController.context)

                return MenuScreenViewModel() as T
            }
        }
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is MenuScreenEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is MenuScreenEvent.NavigateToSettingsScreen -> navController.navigate("SettingsScreen")
                is MenuScreenEvent.NavigateToHistoryScreen -> navController.navigate("HistoryScreen")
                is MenuScreenEvent.NavigateToHomeScreen -> navController.navigate("HomeScreen")
                is MenuScreenEvent.NavigateToQrScreen -> navController.navigate("QrScreen")
                is MenuScreenEvent.NavigateToStatsScreen -> navController.navigate("ProfileScreen")
            }
        }
    }

    // Описание пунктов меню по группам (карточкам) — в том же порядке, что и в макете.
    val groups = listOf(
        listOf(
            MenuButton("Профиль и настройки", viewModel::onSettingsClicked),
            MenuButton("Уведомления") { viewModel.onComingSoonClicked("Уведомления") },
            MenuButton("Способы оплаты") { viewModel.onComingSoonClicked("Способы оплаты") },
            MenuButton("История", viewModel::onHistoryClicked)
        ),
        listOf(
            MenuButton("Новости") { viewModel.onComingSoonClicked("Новости") },
            MenuButton("Акции") { viewModel.onComingSoonClicked("Акции") },
            MenuButton("Промокоды", viewModel::onPromoClicked)
        ),
        listOf(
            MenuButton("Калькулятор экономии") { viewModel.onComingSoonClicked("Калькулятор экономии") },
            MenuButton("Рассчитать выбросы CO₂") { viewModel.onComingSoonClicked("Рассчитать выбросы CO₂") },
        )
    )

    Scaffold(
        containerColor = CustomTrafficWhite,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            AppBottomNavBar(
                items = listOf(
                    NavBarItem(R.drawable.vector_home, "homeIcon", onClick = viewModel::onHomeClicked),
                    NavBarItem(R.drawable.vector_history, "historyIcon", onClick = viewModel::onHistoryClicked),
                    NavBarItem(R.drawable.vector_qr, "qrIcon", onClick = viewModel::onQrClicked),
                    NavBarItem(R.drawable.vector_stats, "statsIcon", onClick = viewModel::onStatsClicked),
                    NavBarItem(R.drawable.vector_more, "moreIcon", onClick = {}, highlighted = true)
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
            item { MenuHeader() }

            groups.forEach { group ->
                item {
                    MenuCard {
                        group.forEachIndexed { index, entry ->
                            MenuItemRow(
                                title = entry.title,
                                showDivider = index != group.lastIndex,
                                onClick = entry.onClick
                            )
                        }
                    }
                    Spacer(Modifier.height(18.dp))
                }
            }

            // Последняя карточка: «О приложении», «Поддержка» и номер телефона
            item {
                MenuCard {
                    MenuItemRow("О приложении", showDivider = true) { viewModel.onComingSoonClicked("О приложении") }
                    MenuItemRow("Поддержка", showDivider = true) { viewModel.onComingSoonClicked("Поддержка") }
                    PhoneRow(
                        phone = SUPPORT_PHONE_DISPLAY,
                        onClick = {
                            try {
                                // ACTION_DIAL только открывает набор номера — разрешение CALL_PHONE не нужно
                                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$SUPPORT_PHONE_DIAL")))
                            } catch (e: ActivityNotFoundException) {
                                scope.launch { snackbarHostState.showSnackbar("Не удалось открыть набор номера") }
                            }
                        }
                    )
                }
            }
        }
    }

    if (uiState.showPromoSheet) {
        ModalBottomSheet(
            onDismissRequest = viewModel::onPromoSheetDismissed,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = CustomTrafficWhite
        ) {
            PromoSheetContent(
                code = uiState.promoCode,
                error = uiState.promoError,
                isApplying = uiState.isApplyingPromo,
                onCodeChanged = viewModel::onPromoCodeChanged,
                onApply = viewModel::onApplyPromoClicked
            )
        }
    }
}

// ============================== Шапка ==============================

@Composable
private fun MenuHeader() {
    Text(
        text = "Меню",
        color = CustomGrey,
        fontFamily = segoe_ui_bold,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
    )
}

// ===================== Карточка с пунктами меню =====================

/**
 * Белая карточка с мягкой бирюзовой тенью (как в макете).
 * ВАЖНО: цвет тени (ambientColor/spotColor) учитывается только на Android 9+ (API 28+);
 * на более старых версиях тень будет обычной серой — это ограничение платформы.
 */
@Composable
private fun MenuCard(content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .shadow(
                elevation = 25.dp,
                shape = shape,
                ambientColor = CustomTurquoiseBlue.copy(alpha = 0.75f),
                spotColor = CustomTurquoiseBlue.copy(alpha = 0.75f)
            )
            .background(Color.White, shape),
        content = content
    )
}

@Composable
private fun MenuItemRow(title: String, showDivider: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = CustomGrey,
            fontFamily = segoe_ui,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )
        Icon(
            painter = painterResource(id = R.drawable.vector_chevron_right),
            contentDescription = null,
            tint = CustomTurquoiseBlue,
            modifier = Modifier.size(22.dp)
        )
    }
    if (showDivider) {
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 18.dp),
            color = CustomGrey.copy(alpha = 0.3f)
        )
    }
}

@Composable
private fun PhoneRow(phone: String, onClick: () -> Unit) {
    Text(
        text = phone,
        color = CustomTurquoiseBlue,
        fontFamily = segoe_ui_bold,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 18.dp)
    )
}

// ======================= Окно "Промокод" =======================

@Composable
private fun PromoSheetContent(
    code: String,
    error: String?,
    isApplying: Boolean,
    onCodeChanged: (String) -> Unit,
    onApply: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            // поднимаем содержимое над клавиатурой, чтобы кнопка не пряталась под ней
            .imePadding()
            .padding(horizontal = 24.dp)
            .padding(top = 8.dp, bottom = 32.dp)
    ) {
        Text(
            text = "Промокод",
            color = CustomGrey,
            fontFamily = segoe_ui_bold,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Введите промокод, чтобы получить скидку или бонус",
            color = CustomGrey.copy(alpha = 0.7f),
            fontFamily = segoe_ui,
            fontSize = 14.sp
        )
        Spacer(Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(50))
                .background(CustomEnterBarColor),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                value = code,
                onValueChange = onCodeChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                textStyle = TextStyle(color = CustomGrey, fontSize = 16.sp, fontFamily = segoe_ui),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onApply() }),
                decorationBox = { innerTextField ->
                    if (code.isEmpty()) {
                        Text(
                            "введите промокод",
                            color = CustomGrey.copy(alpha = 0.6f),
                            fontSize = 16.sp,
                            fontFamily = segoe_ui
                        )
                    }
                    innerTextField()
                }
            )
        }

        if (error != null) {
            Text(
                text = error,
                color = CustomErrorBarBackgroundColor,
                fontFamily = segoe_ui,
                fontSize = 13.sp,
                modifier = Modifier.padding(start = 20.dp, top = 8.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(50))
                .background(CustomTurquoiseBlue)
                .clickable(enabled = !isApplying, onClick = onApply),
            contentAlignment = Alignment.Center
        ) {
            if (isApplying) {
                CircularProgressIndicator(
                    color = CustomTrafficWhite,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Text("Применить", color = CustomTrafficWhite, fontFamily = segoe_ui, fontSize = 18.sp)
            }
        }
    }
}
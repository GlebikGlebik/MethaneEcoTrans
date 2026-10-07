package com.methane.eco.trans.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.methane.eco.trans.theme.CustomTrafficWhite
import com.methane.eco.trans.theme.CustomTurquoiseBlue

/**
 * Один пункт нижней навигации.
 * [highlighted] — слегка увеличивает и делает полностью непрозрачной иконку
 * текущего экрана ("вы здесь"); остальные иконки чуть приглушены (alpha).
 */
data class NavBarItem(
    val iconRes: Int,
    val contentDescription: String,
    val onClick: () -> Unit,
    val highlighted: Boolean = false
)

/**
 * Общая нижняя навигация. Раньше это был приватный HomeBottomNavBar прямо
 * внутри MainScreen.kt — вынес сюда, чтобы не копировать один и тот же
 * Surface+Row на каждом новом экране. Набор иконок передаётся списком,
 * т.к. на разных экранах дизайн показывает разные пункты (сравните нижнее
 * меню MainScreen и HistoryScreen — состав иконок отличается).
 *
 * TODO: MainScreen.kt сейчас всё ещё использует свой старый приватный
 * HomeBottomNavBar/NavIcon — его стоит отдельно смигрировать на этот
 * компонент, чтобы не держать два почти одинаковых куска кода.
 */
@Composable
fun AppBottomNavBar(items: List<NavBarItem>) {
    Surface(color = CustomTurquoiseBlue, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 14.dp, horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                Icon(
                    painter = painterResource(id = item.iconRes),
                    contentDescription = item.contentDescription,
                    tint = if (item.highlighted) Color.White else CustomTrafficWhite.copy(alpha = 0.8f),
                    modifier = Modifier
                        .size(if (item.highlighted) 28.dp else 26.dp)
                        .clickable(onClick = item.onClick)
                )
            }
        }
    }
}
package com.methane.eco.trans.domain.model

data class NavBarItem(
    val iconRes: Int,
    val contentDescription: String,
    val onClick: () -> Unit,
    val highlighted: Boolean = false
)
package com.methane.eco.trans.presentation.regscreen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

data class RegScreenUIState (
    val password: String = "",
    val email: String = "",
    val name: String = "",
    val surname: String = ""
)
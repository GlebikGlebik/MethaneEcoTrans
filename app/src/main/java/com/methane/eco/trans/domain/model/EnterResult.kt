package com.methane.eco.trans.domain.model

sealed class EnterResult {
    object Success : EnterResult()
    data class Error(val message: String) : EnterResult()
}
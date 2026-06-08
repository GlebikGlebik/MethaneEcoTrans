package com.methane.eco.trans.domain.model

sealed class RegResult {
    object Success : RegResult()
    data class Error(val message: String) : RegResult()
}
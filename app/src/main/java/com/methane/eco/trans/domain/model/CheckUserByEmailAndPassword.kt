package com.methane.eco.trans.domain.model

import android.util.Log
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.navigation.NavController
import com.methane.eco.trans.ConnectionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.use

suspend fun checkUserByEmailAndPassword(
    email: String,
    password: String,
    navController: NavController,
    snackbarHostState: SnackbarHostState
): Unit {
    val sqlCommandIfUserExist = """
        SELECT email, password, is_email_verified FROM test_schema_1.user
        WHERE email = ? and password = ?;
    """.trimIndent()
    try{
        val connection = ConnectionManager.getConnection()

        connection.prepareStatement(sqlCommandIfUserExist).use { user ->
            user?.setString(1, email)
            user?.setString(2, password)
            user?.executeQuery().use {executeResult ->
                if (executeResult != null){
                    if (executeResult.next()) {
                        val isVerified = executeResult.getBoolean("is_email_verified")
                        withContext(Dispatchers.Main) {
                            if (isVerified) {
                                navController.navigate("MainScreen") {
                                    popUpTo("EnterScreen") { inclusive = true }
                                }
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Log.e("LoginActivity", "Ошибка авторизации: пользователь не найден")
                            snackbarHostState.showSnackbar(
                                "Пользователь с указанными данными не найден",
                                duration = SnackbarDuration.Long
                            )
                        }
                    }
                }
            }
        }
    }
    catch (e: Exception) {
        withContext(Dispatchers.Main) {
            Log.e("LoginActivity", "Ошибка авторизации: ${e.message}")
            snackbarHostState.showSnackbar(
                "Ошибка входа в аккаунт: ${e.message}",
                duration = SnackbarDuration.Long
            )
        }
    }
}
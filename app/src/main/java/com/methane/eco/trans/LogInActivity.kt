package com.methane.eco.trans

import com.google.firebase.auth.FirebaseAuth
import android.util.Log
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.navigation.NavController
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers


fun signInUser(
    auth: FirebaseAuth,
    email: String,
    password: String,
    navController: NavController,
    snackbarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope
){
    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Проверяем, подтвержден ли email
                val user = auth.currentUser
                if (user?.isEmailVerified == true) {
                    navController.navigate("MainScreen") {
                        popUpTo("EnterScreen") { inclusive = true }
                    }
                } else {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            "Подтвердите email перед входом в аккаунт",
                            duration = SnackbarDuration.Long
                        )
                    }
                    Log.d("LogInActivity", "email пользователя не подтвержден")
                }
            } else {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        "Ошибка входа: ${task.exception?.message ?: "Ошибка входа в аккаунт"}",
                        duration = SnackbarDuration.Long
                    )
                }
                Log.d("LogInActivity", "Ошибка входа в аккаунт")
            }
        }
}
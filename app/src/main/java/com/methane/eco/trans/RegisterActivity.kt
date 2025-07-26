package com.methane.eco.trans

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

suspend fun checkEmailVerification(
    user: FirebaseUser,
    onVerificationSuccess: (FirebaseUser) -> Unit,
    onVerificationFailure: (String) -> Unit
) {

    // ждем подтверждения почты от пользователя в течение 1 минуты (60000 мс)
    val startTime = System.currentTimeMillis()
    val timeout = 60000L // 1 минута
    val interval = 5000L // 5 секунд

    while (System.currentTimeMillis() - startTime < timeout){
        user.reload() // Обновляем информацию о пользователе
        if (user.isEmailVerified) {
            onVerificationSuccess(user) // Если подтверждено, вызываем успех
            return
        }
        delay(interval)
    }
    // Если время вышло, удаляем пользователя
    user.delete()
    onVerificationFailure("Ошибка: пользователь не подтвердил свой email")
}

@Composable
fun RegisterActivity(
    email: String,
    password: String,
    onRegistrationSuccess: (FirebaseUser) -> Unit,
    onRegistrationFailure: (String) -> Unit
) {
    val auth = FirebaseAuth.getInstance()

    LaunchedEffect(Unit) {
        try {
            // Регистрируем пользователя в FireBas
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            // Создаем экземпляр user класса FirebaseUser дл использования его методов
            val user = result.user

            // Отправка письма с подтверждением
            user?.sendEmailVerification()?.await()

            //ждем пока пользователь подтвердит почту
            if (user != null) {
                checkEmailVerification(user, onRegistrationSuccess, onRegistrationFailure)
            } else {
                onRegistrationFailure("Ошибка: пользователь не создан")
            }
        } catch (e: Exception) {
            Log.e("RegisterActivity", "Ошибка регистрации: ${e.message}")
            onRegistrationFailure("Ошибка регистрации: ${e.message}")
        }
    }
}

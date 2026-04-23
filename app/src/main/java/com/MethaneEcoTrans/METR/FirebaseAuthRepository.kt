package com.MethaneEcoTrans.METR

import android.util.Log
import android.util.Patterns
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.suspendCancellableCoroutine

class FirebaseAuthRepository(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseDatabase: FirebaseDatabase
) : AuthRepository {

    // добавляем функцию, проверяющую корректность email
    override fun isEmailValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    override suspend fun checkEmailVerification(
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

    override suspend fun registerUser(
        email: String,
        password: String,
        onRegistrationSuccess: (FirebaseUser) -> Unit,
        onRegistrationFailure: (String) -> Unit
    ): Result<FirebaseUser> = suspendCancellableCoroutine { continuable ->
        val auth = FirebaseAuth.getInstance()
        val scope = CoroutineScope(Dispatchers.Main + Job())
        // Регистрируем пользователя в FireBas
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val user = result.user
                // запускаем корутину
                scope.launch {

                    try {
                        // Отправка письма с подтверждением
                        user?.sendEmailVerification()?.await()
                        if (user != null) {
                            checkEmailVerification(user, onRegistrationSuccess, onRegistrationFailure)
                        } else {
                            onRegistrationFailure("Ошибка: пользователь не создан")
                        }
                    } catch (e: Exception) {
                        Log.e("RegisterBack", "Ошибка регистрации: ${e.message}")
                        onRegistrationFailure("Ошибка регистрации: ${e.message}")
                    }
                }
            }
        }
    override fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser
}
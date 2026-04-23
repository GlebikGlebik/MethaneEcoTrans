package com.MethaneEcoTrans.METR

import com.google.firebase.auth.FirebaseUser

interface AuthRepository {

    suspend fun registerUser(
        email: String,
        password: String,
        onRegistrationSuccess: (FirebaseUser) -> Unit,
        onRegistrationFailure: (String) -> Unit
    ): Result<FirebaseUser>

    suspend fun checkEmailVerification(
        user: FirebaseUser,
        onVerificationSuccess: (FirebaseUser) -> Unit,
        onVerificationFailure: (String) -> Unit
    )

    fun getCurrentUser(): FirebaseUser?

    fun isEmailValid(email: String): Boolean
}
package com.methane.eco.trans.data.repository

import android.util.Log
import com.methane.eco.trans.data.dto.*
import com.methane.eco.trans.data.local.TokenStorage
import com.methane.eco.trans.domain.model.AuthResult
import com.methane.eco.trans.domain.repository.AuthRepository
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class AuthRepositoryImpl(
    private val tokenStorage: TokenStorage
) : AuthRepository {
    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.BODY
        }
        engine {
            connectTimeout = 30_000
            socketTimeout = 30_000
        }
    }

    private val baseUrl = "http://10.0.2.2:8080"

    override suspend fun signIn(
        email: String,
        password: String,
        companyInn: String?
    ): AuthResult {
        return try {
            Log.d("AuthRepository", "Отправка запроса на вход: $email")

            val requestBody = mutableMapOf(
                "email" to email,
                "password" to password
            )

            // Добавляем companyInn если это B2B
            companyInn?.let { inn ->
                requestBody["companyInn"] = inn
            }

            val response: HttpResponse = client.post("$baseUrl/api/v1/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

            Log.d("AuthRepository", "Статус ответа: ${response.status}")

            when (response.status) {
                HttpStatusCode.OK -> {
                    val authResponse: AuthResponse = response.body()
                    Log.d("AuthRepository", "Успешный вход: ${authResponse.email}")

                    tokenStorage.saveAuthToken(
                        token = authResponse.token,
                        userId = authResponse.userId,
                        email = authResponse.email,
                        firstName = authResponse.firstName,
                        lastName = authResponse.lastName
                    )

                    AuthResult.Success
                }
                HttpStatusCode.Unauthorized -> {
                    val errorResponse: ErrorResponse = response.body()
                    Log.e("AuthRepository", "Ошибка входа: ${errorResponse.message}")
                    AuthResult.Error(errorResponse.message)
                }
                else -> {
                    val errorMessage = "Ошибка сервера: ${response.status}"
                    Log.e("AuthRepository", errorMessage)
                    AuthResult.Error(errorMessage)
                }
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Исключение при входе: ${e.message}", e)
            AuthResult.Error(
                when {
                    e.message?.contains("timeout") == true -> "Превышено время ожидания. Проверьте подключение к серверу."
                    e.message?.contains("connection refused") == true -> "Сервер недоступен. Убедитесь, что сервер запущен."
                    else -> "Ошибка подключения: ${e.message ?: "Неизвестная ошибка"}"
                }
            )
        }
    }

    override suspend fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phone: String?,
        userType: String, // ✅ НОВОЕ
        companyInn: String? // ✅ НОВОЕ
    ): AuthResult {
        return try {
            val response: HttpResponse = client.post("$baseUrl/api/v1/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(RegisterRequest(
                    email = email,
                    password = password,
                    firstName = firstName,
                    lastName = lastName,
                    phone = phone,
                    userType = userType, // ✅ НОВОЕ
                    companyInn = companyInn // ✅ НОВОЕ
                ))
            }
            when (response.status) {
                HttpStatusCode.Created -> {
                    val authResponse: AuthResponse = response.body()
                    tokenStorage.saveAuthToken(
                        token = authResponse.token,
                        userId = authResponse.userId,
                        email = authResponse.email,
                        firstName = authResponse.firstName,
                        lastName = authResponse.lastName
                    )
                    AuthResult.Success
                }
                HttpStatusCode.BadRequest -> {
                    val errorResponse: ErrorResponse = response.body()
                    AuthResult.Error(errorResponse.message)
                }
                else -> {
                    AuthResult.Error("Ошибка сервера: ${response.status}")
                }
            }
        } catch (e: Exception) {
            AuthResult.Error("Ошибка подключения: ${e.message ?: "Неизвестная ошибка"}")
        }
    }
}
package com.methane.eco.trans.data.repository

import android.util.Log
import com.methane.eco.trans.data.dto.AuthResponse
import com.methane.eco.trans.data.dto.ErrorResponse
import com.methane.eco.trans.data.dto.LoginRequest
import com.methane.eco.trans.data.dto.RegisterRequest
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

    // Ktor Client для HTTP запросов
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
        // Устанавливаем базовый URL сервера
        // Для эмулятора Android: 10.0.2.2 (localhost)
        // Для реального устройства: 192.168.0.101
        engine {
            connectTimeout = 30_000 // 30 секунд
            socketTimeout = 30_000
        }
    }

    // Базовый URL сервера
    private val baseUrl = "http://10.0.2.2:8080" // Для эмулятора
    // private val baseUrl = "http://192.168.0.101:8080" // Для реального устройства (замените IP)

    override suspend fun signIn(email: String, password: String): AuthResult {
        return try {
            Log.d("AuthRepository", "Отправка запроса на вход: $email")

            val response: HttpResponse = client.post("$baseUrl/api/v1/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(email, password))
            }

            Log.d("AuthRepository", "Статус ответа: ${response.status}")

            when (response.status) {
                HttpStatusCode.OK -> {
                    val authResponse: AuthResponse = response.body()
                    Log.d("AuthRepository", "Успешный вход: ${authResponse.email}")

                    // Сохраняем токен и данные пользователя
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

    // Метод регистрации (добавим в интерфейс AuthRepository)
    override suspend fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phone: String?
    ): AuthResult {
        return try {
            Log.d("AuthRepository", "Отправка запроса на регистрацию: $email")

            val response: HttpResponse = client.post("$baseUrl/api/v1/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(RegisterRequest(email, password, firstName, lastName, phone))
            }

            Log.d("AuthRepository", "Статус ответа: ${response.status}")

            when (response.status) {
                HttpStatusCode.Created -> {
                    val authResponse: AuthResponse = response.body()
                    Log.d("AuthRepository", "Успешная регистрация: ${authResponse.email}")

                    // Сохраняем токен и данные пользователя
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
                    Log.e("AuthRepository", "Ошибка регистрации: ${errorResponse.message}")
                    AuthResult.Error(errorResponse.message)
                }
                else -> {
                    val errorMessage = "Ошибка сервера: ${response.status}"
                    Log.e("AuthRepository", errorMessage)
                    AuthResult.Error(errorMessage)
                }
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Исключение при регистрации: ${e.message}", e)
            AuthResult.Error(
                when {
                    e.message?.contains("timeout") == true -> "Превышено время ожидания. Проверьте подключение к серверу."
                    e.message?.contains("connection refused") == true -> "Сервер недоступен. Убедитесь, что сервер запущен."
                    else -> "Ошибка подключения: ${e.message ?: "Неизвестная ошибка"}"
                }
            )
        }
    }
}
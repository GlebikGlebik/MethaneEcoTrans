package com.methane.eco.trans.data.repository

import android.util.Log
import com.methane.eco.trans.data.dto.*
import com.methane.eco.trans.data.local.TokenStorage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json

class MainRepositoryImpl(
    private val tokenStorage: TokenStorage
) : MainRepository {

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        install(Logging) {
            level = LogLevel.ALL
        }
    }

    private val baseUrl = "http://10.0.2.2:8080"

    private suspend fun getToken(): String? {
        return tokenStorage.authToken.first()
    }

    override suspend fun getVehicles(): Result<List<VehicleDto>> {
        return try {
            val token = getToken() ?: return Result.failure(Exception("Не авторизован"))
            val response: HttpResponse = client.get("$baseUrl/api/v1/users/vehicles") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            when (response.status) {
                HttpStatusCode.OK -> {
                    val vehiclesResponse: VehiclesResponse = response.body()
                    Result.success(vehiclesResponse.vehicles)
                }
                else -> Result.failure(Exception("Ошибка: ${response.status}"))
            }
        } catch (e: Exception) {
            Log.e("MainRepository", "Ошибка получения ТС: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun addVehicle(request: CreateVehicleRequest): Result<String> {
        return try {
            val token = getToken() ?: return Result.failure(Exception("Не авторизован"))
            val response: HttpResponse = client.post("$baseUrl/api/v1/users/vehicles") {
                contentType(ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer $token")
                setBody(request)
            }
            when (response.status) {
                HttpStatusCode.Created -> Result.success("ТС добавлено")
                else -> Result.failure(Exception("Ошибка: ${response.status}"))
            }
        } catch (e: Exception) {
            Log.e("MainRepository", "Ошибка добавления ТС: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteVehicle(vehicleId: String): Result<Unit> {
        return try {
            val token = getToken() ?: return Result.failure(Exception("Не авторизован"))
            val response: HttpResponse = client.delete("$baseUrl/api/v1/users/vehicles/$vehicleId") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            when (response.status) {
                HttpStatusCode.OK -> Result.success(Unit)
                else -> Result.failure(Exception("Ошибка: ${response.status}"))
            }
        } catch (e: Exception) {
            Log.e("MainRepository", "Ошибка удаления ТС: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun addRefueling(request: CreateRefuelingRequest): Result<String> {
        return try {
            val token = getToken() ?: return Result.failure(Exception("Не авторизован"))
            val response: HttpResponse = client.post("$baseUrl/api/v1/refueling") {
                contentType(ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer $token")
                setBody(request)
            }
            when (response.status) {
                HttpStatusCode.Created -> Result.success("Заправка добавлена")
                else -> Result.failure(Exception("Ошибка: ${response.status}"))
            }
        } catch (e: Exception) {
            Log.e("MainRepository", "Ошибка добавления заправки: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ОБНОВЛЕННАЯ РЕАЛИЗАЦИЯ ПОЛУЧЕНИЯ ИСТОРИИ
    override suspend fun getRefuelingHistory(
        vehicleId: String?,
        startDate: String?,
        endDate: String?,
        page: Int,
        size: Int
    ): Result<RefuelingHistoryResponse> {
        return try {
            val token = getToken() ?: return Result.failure(Exception("Не авторизован"))

            // Динамическая сборка URL с query-параметрами
            val url = URLBuilder("$baseUrl/api/v1/refueling/history").apply {
                vehicleId?.let { parameters.append("vehicleId", it) }
                startDate?.let { parameters.append("startDate", it) }
                endDate?.let { parameters.append("endDate", it) }
                parameters.append("page", page.toString())
                parameters.append("size", size.toString())
            }.buildString()

            val response: HttpResponse = client.get(url) {
                header(HttpHeaders.Authorization, "Bearer $token")
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    val historyResponse: RefuelingHistoryResponse = response.body()
                    Result.success(historyResponse)
                }
                else -> Result.failure(Exception("Ошибка сервера: ${response.status}"))
            }
        } catch (e: Exception) {
            Log.e("MainRepository", "Ошибка получения истории: ${e.message}", e)
            Result.failure(e)
        }
    }
}
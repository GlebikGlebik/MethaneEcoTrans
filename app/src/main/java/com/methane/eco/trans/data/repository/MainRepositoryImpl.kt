package com.methane.eco.trans.data.repository

import android.util.Log
import com.methane.eco.trans.data.dto.CreateRefuelingRequest
import com.methane.eco.trans.data.dto.CreateVehicleRequest
import com.methane.eco.trans.data.dto.RefuelingDto
import com.methane.eco.trans.data.dto.RefuelingHistoryResponse
import com.methane.eco.trans.data.dto.VehicleDto
import com.methane.eco.trans.data.dto.VehiclesResponse
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
    // Устанавливаем базовый URL сервера
    // Для эмулятора Android: 10.0.2.2 (localhost)
    // Для реального устройства: 192.168.0.101
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
                HttpStatusCode.Created -> {
                    val body = response.bodyAsText()
                    Result.success("ТС добавлено")
                }
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
                HttpStatusCode.Created -> {
                    val body = response.bodyAsText()
                    Result.success("Заправка добавлена")
                }
                else -> Result.failure(Exception("Ошибка: ${response.status}"))
            }
        } catch (e: Exception) {
            Log.e("MainRepository", "Ошибка добавления заправки: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun getRefuelingHistory(vehicleId: String?): Result<List<RefuelingDto>> {
        return try {
            val token = getToken() ?: return Result.failure(Exception("Не авторизован"))

            val url = if (vehicleId != null) {
                "$baseUrl/api/v1/refueling/history?vehicleId=$vehicleId"
            } else {
                "$baseUrl/api/v1/refueling/history"
            }

            val response: HttpResponse = client.get(url) {
                header(HttpHeaders.Authorization, "Bearer $token")
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    val historyResponse: RefuelingHistoryResponse = response.body()
                    Result.success(historyResponse.refuelings)
                }
                else -> Result.failure(Exception("Ошибка: ${response.status}"))
            }
        } catch (e: Exception) {
            Log.e("MainRepository", "Ошибка получения истории: ${e.message}", e)
            Result.failure(e)
        }
    }
}
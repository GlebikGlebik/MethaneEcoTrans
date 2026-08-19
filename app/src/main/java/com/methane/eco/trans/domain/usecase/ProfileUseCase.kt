package com.methane.eco.trans.domain.usecase

import com.met.server.data.dto.UserProfileResponse
import com.methane.eco.trans.data.dto.RefuelingHistoryResponse
import com.methane.eco.trans.data.dto.VehicleDto
import com.methane.eco.trans.data.repository.MainRepository
import com.methane.eco.trans.domain.model.RefuelingRecord
import com.methane.eco.trans.domain.model.UserProfile
import com.methane.eco.trans.domain.model.VehicleStats
import com.methane.eco.trans.domain.repository.ProfileRepository
import java.time.LocalDate

class ProfileUseCase(private val profileRepository: ProfileRepository, val mainRepository: MainRepository) {
    suspend fun getUserProfile(): Result<UserProfileResponse> {
        return profileRepository.getProfile()
    }

    suspend fun getUserVehicles(): Result<List<VehicleDto>>{
        return mainRepository.getVehicles()
    }

    suspend fun getUserRefuelingHistory(): Result<RefuelingHistoryResponse>{
        return mainRepository.getRefuelingHistory()
    }

    fun calculateVehicleStats(
        history: Map<String, List<RefuelingRecord>>,
        currentVehicle: String
    ): VehicleStats {
        val records = history[currentVehicle] ?: emptyList()

        if (records.isEmpty()) return VehicleStats()

        // 1. Считаем общие суммы
        val totalFuel = records.sumOf { it.volume }
        val totalSum = records.sumOf { it.sum }

        // 2. Берем последние 6 заправок (сортируем по дате)
        val latestRefills = records.sortedByDescending { parseDateToLong(it.date) }.take(6)

        // 3. Считаем статистику по месяцам
        val now = LocalDate.now()
        val currentMonth = now.monthValue
        val currentYear = now.year
        val prevMonth = if (currentMonth == 1) 12 else currentMonth - 1
        val prevYear = if (currentMonth == 1) currentYear - 1 else currentYear

        var currentVol = 0.0
        var currentSm = 0.0
        var prevVol = 0.0
        var prevSm = 0.0

        records.forEach { record ->
            val parts = record.date.split(";")
            if (parts.size == 3) {
                val month = parts[1].toIntOrNull() ?: 0
                val year = parts[2].toIntOrNull() ?: 0

                when {
                    month == currentMonth && year == currentYear -> {
                        currentVol += record.volume
                        currentSm += record.sum
                    }
                    month == prevMonth && year == prevYear -> {
                        prevVol += record.volume
                        prevSm += record.sum
                    }
                }
            }
        }

        return VehicleStats(
            totalFuel = totalFuel,
            totalSum = totalSum,
            currentMonthStats = Pair(currentVol, currentSm),
            previousMonthStats = Pair(prevVol, prevSm),
            monthlyComparison = Pair(currentVol - prevVol, currentSm - prevSm),
            latestRefills = latestRefills
        )
    }

    /**
     * Вспомогательная функция для сортировки дат вида "dd;MM;yyyy"
     * Превращает дату в число для корректной сортировки
     */
    private fun parseDateToLong(dateStr: String): Long {
        return try {
            val parts = dateStr.split(";")
            val day = parts[0].toLong()
            val month = parts[1].toLong()
            val year = parts[2].toLong()
            year * 10000 + month * 100 + day
        } catch (e: Exception) {
            0L
        }
    }

}
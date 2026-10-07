package com.methane.eco.trans.presentation.historyscreen

import java.time.LocalDate
import java.time.YearMonth

/**
 * Быстрые пресеты фильтра "Период" из дизайна.
 *
 * TODO: когда понадобится точный произвольный диапазон дат — добавить
 * полноценный выбор диапазона (Material3 DatePicker/DateRangePicker) и
 * хранить startDate/endDate напрямую в UI-состоянии вместо enum.
 */
enum class PeriodFilter(val label: String) {
    ALL("Весь период"),
    THIS_MONTH("Этот месяц"),
    LAST_MONTH("Прошлый месяц")
}

/**
 * Переводит пресет в границы периода в формате "yyyy-MM-dd", который уже
 * умеет принимать GetRefuelingHistoryUseCase(startDate, endDate) — новый
 * backend-эндпоинт заводить не пришлось, фильтр полностью ложится на
 * существующий API.
 */
fun PeriodFilter.toDateRange(today: LocalDate = LocalDate.now()): Pair<String?, String?> {
    return when (this) {
        PeriodFilter.ALL -> null to null
        PeriodFilter.THIS_MONTH -> {
            val month = YearMonth.from(today)
            month.atDay(1).toString() to month.atEndOfMonth().toString()
        }
        PeriodFilter.LAST_MONTH -> {
            val month = YearMonth.from(today).minusMonths(1)
            month.atDay(1).toString() to month.atEndOfMonth().toString()
        }
    }
}
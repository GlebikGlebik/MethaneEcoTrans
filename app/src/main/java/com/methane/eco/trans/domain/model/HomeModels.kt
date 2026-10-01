package com.methane.eco.trans.domain.model

import com.methane.eco.trans.R

/**
 * Загрузку картинки по URL нужно будет подключить отдельно — рекомендуется
 * библиотека Coil (io.coil-kt:coil-compose), в HomeActionCard уже оставлена
 * точка подключения (см. комментарий TODO в MainScreen.kt).
 */
data class HomeActionItem(
    val id: String,
    val title: String,
    val icon: Int? = null,
    val imageUrl: String? = null
)

/**
 * Заглушки-плейсхолдеры для секции "Сервисы".
 *
 * TODO: как только на бэкенде появится реальный эндпоинт (например,
 * GET /api/v1/services), эти данные нужно будет загружать через новый
 * UseCase/Repository в MainScreenViewModel, а не хранить как значения
 * по умолчанию в UI-состоянии.
 */
fun defaultServiceItems(): List<HomeActionItem> = listOf(
    HomeActionItem(id = "to_gas", title = "Перейти на газ", icon = R.drawable.vector_wrench),
    HomeActionItem(id = "to_service", title = "Записаться на ТО", icon = R.drawable.vector_car_service),
    HomeActionItem(id = "to_wash", title = "Мойка", icon = R.drawable.vector_wash)
)

/**
 * Заглушки-плейсхолдеры для секции "Новости".
 *
 * Пока нет готовых изображений — используем тот же иконочный стиль, что и у
 * "Сервисы" (как вы и планировали). Как только появятся картинки — просто
 * замените icon на imageUrl в каждом элементе, верстка не потребует правок.
 *
 * TODO: заменить на загрузку через GET /api/v1/news, когда появится API.
 */
fun defaultNewsItems(): List<HomeActionItem> = listOf(
    HomeActionItem(id = "news_promo", title = "Новая акция\nдля клиентов", icon = R.drawable.vector_news),
    HomeActionItem(id = "news_station", title = "Открытие\nновой станции", icon = R.drawable.vector_news)
)
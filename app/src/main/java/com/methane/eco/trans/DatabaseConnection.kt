package com.methane.eco.trans

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import android.util.Log
import org.postgresql.Driver
import java.sql.Statement

object ConnectionManager {
    private const val PASSWORD = "Plmnko_20014"
    private const val USERNAME = "postgres"
    private const val URL = "jdbc:postgresql://localhost:5432/test_database_1"

    private val driverClass = Driver::class.java

    val connection: Connection = open()
    val statement: Statement = connection.createStatement()

    fun open(): Connection {
        return try {
            DriverManager.getConnection(URL, USERNAME, PASSWORD)
        } catch (e: SQLException) {
            Log.e("DatabaseConnection", "Ошибка подключения к БД: ${e.message}")
            throw RuntimeException("Failed to connect to the database", e)
        }
    }
}
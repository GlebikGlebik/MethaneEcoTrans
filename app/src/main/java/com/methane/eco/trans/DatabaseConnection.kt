package com.methane.eco.trans

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import android.util.Log
import androidx.compose.material3.SnackbarHostState
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import java.sql.Statement
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object ConnectionManager {
    private const val PASSWORD = "Plmnko_20014"
    private const val USERNAME = "postgres"
    private const val URL = "jdbc:postgresql://192.168.0.88:5432/test_database_1"

    @Volatile
    private var _connection: Connection? = null
    @Volatile
    private var _statement: Statement? = null

    init {
        System.setProperty("org.postgresql.disable.management.factory", "true")
        System.setProperty("org.postgresql.disable.gssapi", "true")
        try {
            // Для Android-совместимого драйвера
            Class.forName("org.postgresql.Driver")
        } catch (e: ClassNotFoundException) {
            throw RuntimeException("PostgreSQL JDBC Driver not found", e)
        }
    }

    suspend fun getConnection(): Connection = withContext(Dispatchers.IO) {
        _connection?: DriverManager.getConnection(URL, USERNAME, PASSWORD).also {
            _connection = it
            //Log.d("DatabaseConnection", "connection created")
        }
    }

    suspend fun getStatement(): Statement = withContext(Dispatchers.IO) {
        (_connection ?: getConnection()).createStatement().also {
            _statement = it
            //Log.d("DatabaseConnection", "statement created")
        }
    }
}

fun main() = runBlocking(Dispatchers.IO) {
    val statement = ConnectionManager.getStatement()
    println(statement.execute("SELECT * FROM test_schema_1.user"))
}



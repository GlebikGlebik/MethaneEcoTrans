package com.methane.eco.trans

import org.postgresql.Driver
import java.sql.Connection
import java.sql.SQLException

val driverClass = Driver::class.java

fun main() {
    ConnectionManager.open().use { connection -> println(connection.transactionIsolation) }
}

fun getConnection(){

}

package com.klejdis.services.config

import com.klejdis.services.MODE
import com.klejdis.services.Mode
import com.klejdis.services.model.*

import com.klejdis.services.util.parseSqlFile
import org.ktorm.database.Database
import org.ktorm.entity.sequenceOf
import org.ktorm.support.postgresql.PostgreSqlDialect

private fun configuredDB(): Database {
    val env = System.getenv()
    val database = Database.connect(
        url = "jdbc:postgresql://${env["POSTGRES_HOST"]}:${env["POSTGRES_PORT"]}/${env["POSTGRES_DATABASE"]}",
        driver = "org.postgresql.Driver",
        user = env["POSTGRES_USER"].toString(),
        password = env["POSTGRES_PASSWORD"].toString(),
        dialect = PostgreSqlDialect()
    )
    return database
}

val postgresDatabase: Database by lazy { configuredDB() }
val Database.items get() = this.sequenceOf(Items)
val Database.itemTypes get() = this.sequenceOf(ItemTypes)
val Database.accounts get() = this.sequenceOf(Businesses)
val Database.orders get() = this.sequenceOf(Orders)
val Database.expenses get() = this.sequenceOf(Expenses)
val Database.customers get() = this.sequenceOf(Customers)

private object DBService {
    private fun executeSqlFile(fileName: String) {
        postgresDatabase.useConnection { conn ->
            val sql = parseSqlFile(fileName)
            conn.createStatement().execute(sql)
        }
    }

    fun createTables() {
        executeSqlFile("create_tables")
        println("Tables created successfully")
    }

    fun dropTables() {
        executeSqlFile("drop_tables")
        println("Tables dropped successfully")
    }

    fun populateTables() {
        executeSqlFile("populate_tables")
        println("Tables populated successfully")
    }

}

/**
 * Populates the database with the initial data, only in DEV mode for testing purposes.
 * Always rebuilds the database in DEV mode, does nothing in PROD mode.
 */
fun rebuildDatabase() {
    if (MODE == Mode.DEV) {
        DBService.dropTables()
        DBService.createTables()
        DBService.populateTables()
    }
}

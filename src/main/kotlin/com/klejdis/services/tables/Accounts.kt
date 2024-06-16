package com.klejdis.services.tables

import com.klejdis.services.model.Account
import org.ktorm.dsl.QueryRowSet
import org.ktorm.schema.BaseTable
import org.ktorm.schema.int
import org.ktorm.schema.varchar

object Accounts: BaseTable<Account>("accounts") {
    val id = int("id").primaryKey()
    val username = varchar("username")
    val password = varchar("password")
    val salt = varchar("salt")
    override fun doCreateEntity(row: QueryRowSet, withReferences: Boolean): Account {
        return Account(
            id = row[id] ?: 0,
            username = row[username] ?: "",
            password = row[password] ?: "",
            salt = row[salt] ?: ""
        )
    }

}
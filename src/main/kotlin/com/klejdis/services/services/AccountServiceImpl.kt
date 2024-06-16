package com.klejdis.services.services

import com.klejdis.services.config.postgresDatabase
import com.klejdis.services.model.Account
import com.klejdis.services.tables.Accounts
import com.klejdis.services.util.PasswordUtil
import org.ktorm.dsl.*
import org.postgresql.util.PSQLException
import org.postgresql.util.PSQLState

class AccountServiceImpl : AccountService {

    override suspend fun get(id: Int): Account? {
        val account = postgresDatabase
            .from(Accounts)
            .select()
            .where { Accounts.id eq id }
            .map { row -> Accounts.createEntity(row) }
            .firstOrNull()
        return account
    }

    override suspend fun getByUsername(username: String): Account? {
        val account = postgresDatabase
            .from(Accounts)
            .select()
            .where { Accounts.username eq username }
            .map { row -> Accounts.createEntity(row) }
            .firstOrNull()
        return account
    }

    override suspend fun create(account: Account): Account {
        val salt = PasswordUtil.generateSalt()
        val hashedPassword = PasswordUtil.hashPassword(account.password, salt)
        val newAccount = account.copy(password = hashedPassword, salt = salt)
        try {
            val id = postgresDatabase.insertAndGenerateKey(Accounts) {
                set(it.username, newAccount.username)
                set(it.password, newAccount.password)
                set(it.salt, newAccount.salt)
            } as Int
            return newAccount.copy(id = id)
        }
        catch (e: PSQLException) {
            when (e.sqlState) {
                PSQLState.UNIQUE_VIOLATION.state -> throw EntityAlreadyExistsException(
                    "Account with username ${account.username} already exists."
                )
                else -> throw e
            }
        }
    }

    override suspend fun update(id: Int): Account {
        return Account(0, "", "")
    }

    override suspend fun delete(id: Int): Boolean {
        return false
    }
}
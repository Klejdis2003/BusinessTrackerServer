package com.klejdis.services.services

import com.klejdis.services.model.Account
import com.klejdis.services.util.PasswordUtil
import kotlin.jvm.Throws

class AuthenticationServiceImpl(
    private val accountService: AccountService
): AuthenticationService {

    override suspend fun register(account: Account): Account {
        val salt = PasswordUtil.generateSalt()
        val hashedPassword = PasswordUtil.hashPassword(account.password, salt)
        return account.copy(password = hashedPassword, salt = salt)
    }

    override suspend fun authenticate(username: String, password: String): AuthStatus {
        val account = accountService.getByUsername(username) ?: return AuthStatus.INVALID_USERNAME
        val hashedPassword = PasswordUtil.hashPassword(password, account.salt)
        return if (hashedPassword == account.password) AuthStatus.SUCCESS else AuthStatus.INVALID_PASSWORD
    }


}


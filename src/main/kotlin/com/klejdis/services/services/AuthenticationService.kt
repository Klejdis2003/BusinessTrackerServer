package com.klejdis.services.services

import com.klejdis.services.model.Account

interface AuthenticationService {
    suspend fun register(account: Account): Account
    suspend fun authenticate(username: String, password: String): AuthStatus

}

enum class AuthStatus(message: String){
    SUCCESS("Login successful"),
    INVALID_USERNAME("Invalid username"),
    INVALID_PASSWORD("Invalid password")
}
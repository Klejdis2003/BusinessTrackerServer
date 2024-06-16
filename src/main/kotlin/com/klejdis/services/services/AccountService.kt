package com.klejdis.services.services

import com.klejdis.services.model.Account

interface AccountService: Service<Account> {
    suspend fun getByUsername(username: String): Account?
}
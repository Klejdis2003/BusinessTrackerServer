package com.klejdis.services


import com.klejdis.services.config.JwtConfig
import com.klejdis.services.services.AccountService
import com.klejdis.services.services.AccountServiceImpl
import com.klejdis.services.services.AuthenticationService
import com.klejdis.services.services.AuthenticationServiceImpl
import io.github.cdimascio.dotenv.Dotenv
import org.dotenv.vault.dotenvVault
import org.koin.dsl.module

val appModule = module {
    single<AccountService> { AccountServiceImpl() }
    single<AuthenticationService> { AuthenticationServiceImpl(get()) }
    single<Dotenv> { dotenvVault() }
    single<JwtConfig> { JwtConfig(get()) }
}
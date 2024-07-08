package com.klejdis.services.plugins

import com.klejdis.services.appModule
import com.klejdis.services.services.OAuthenticationService
import io.ktor.server.application.*
import org.koin.ktor.ext.getKoin
import org.koin.ktor.plugin.Koin

fun Application.configureKoin() {
    install(Koin) {
        modules(appModule)
        getKoin().get<OAuthenticationService>() // initialize the service
    }

}
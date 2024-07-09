package com.klejdis.services.plugins

import com.klejdis.services.appModule
import io.ktor.server.application.*
import org.koin.ktor.plugin.Koin

fun Application.configureKoin() {
    install(Koin) { modules(appModule) }

}
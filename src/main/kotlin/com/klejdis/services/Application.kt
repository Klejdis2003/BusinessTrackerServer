package com.klejdis.services

import com.klejdis.services.config.rebuildDatabase
import com.klejdis.services.plugins.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

val MODE = Mode.DEV

fun main() {
    embeddedServer(Netty, port = 8080, host = "localhost", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureSecurity()
    configureSerialization()
    configureRouting()
    rebuildDatabase()
}

enum class Mode {
    DEV, PROD
}

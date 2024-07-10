package com.klejdis.services.plugins

import com.klejdis.services.model.Session
import com.klejdis.services.services.OAuthenticationService
import io.ktor.server.application.*
import io.ktor.server.sessions.*
import org.koin.ktor.ext.inject
import java.io.File

/**
 * 10 minutes, represented in seconds
 */
const val sessionMaxAgeInSeconds: Long = 60 * 10 // 10 minutes

fun Application.configureSessions() {
    val oAuthenticationService by inject<OAuthenticationService>()
    install(Sessions) {
        cookie<Session>("LoginSession", directorySessionStorage(File("build/.sessions"), cached = true)) {
            cookie.path = "/"
            cookie.maxAgeInSeconds = sessionMaxAgeInSeconds
            cookie.secure = true
            cookie.httpOnly = true
        }
    }
    monitor.subscribe(ApplicationStopping) {
        oAuthenticationService.destroy()
        println("Session monitor stopping")
    }
}
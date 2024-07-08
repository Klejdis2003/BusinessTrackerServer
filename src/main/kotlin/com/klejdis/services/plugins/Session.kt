package com.klejdis.services.plugins

import com.klejdis.services.model.Session
import com.klejdis.services.services.OAuthenticationService
import io.ktor.server.application.*
import io.ktor.server.sessions.*
import kotlinx.html.A
import org.koin.ktor.ext.inject

/**
 * 10 minutes, represented in seconds
 */
const val sessionMaxAgeInSeconds: Long = 60 * 10

fun Application.configureSessions() {
    val oAuthenticationService by inject<OAuthenticationService>()
    install(Sessions) {
        cookie<Session>("LoginSession") {
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
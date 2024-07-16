package com.klejdis.services.plugins

import com.klejdis.services.model.Session
import com.klejdis.services.services.OAuthenticationService
import io.ktor.server.application.*
import io.ktor.server.sessions.*
import org.koin.ktor.ext.inject
import java.io.File

/**
 * The maximum age of a session in seconds. Defaults to 120 minutes.
 * The value is read from the environment variable SESSION_MAX_AGE, which is expected to be in minutes.
 */
val SESSION_MAX_AGE_SECONDS: Long = (System.getenv("SESSION_MAX_AGE")?.toLong() ?: 120) * 60

fun Application.configureSessions() {
    val oAuthenticationService by inject<OAuthenticationService>()
    install(Sessions) {
        cookie<Session>("LoginSession", directorySessionStorage(File("build/.sessions"), cached = true)) {
            cookie.path = "/"
            cookie.maxAgeInSeconds = SESSION_MAX_AGE_SECONDS
            cookie.secure = true
            cookie.httpOnly = true
            cookie.extensions["SameSite"] = "Lax"

        }
    }
    monitor.subscribe(ApplicationStopping) {
        oAuthenticationService.destroy()
        println("Session monitor stopping")
    }
}
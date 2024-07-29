package com.klejdis.services.plugins

import com.klejdis.services.model.LoginSession
import com.klejdis.services.routes.LoginMethod
import com.klejdis.services.services.OAuthenticationService
import io.ktor.server.application.*
import io.ktor.server.sessions.*
import org.koin.ktor.ext.inject
import java.io.File

/**
 * The maximum age of a session in seconds. Defaults to 120 minutes.
 * The value is read from the environment variable SESSION_MAX_AGE, which is expected to be in minutes.
 * By default, the value is 120 minutes, and it can be overridden by setting the environment variable.
 */
val LOGIN_SESSION_MAX_AGE_SECONDS: Long = (System.getenv("SESSION_MAX_AGE")?.toLong() ?: 120) * 60

fun Application.configureSessions() {
    val oAuthenticationService by inject<OAuthenticationService>()
    install(Sessions) {
        cookie<LoginSession>("LoginSession", directorySessionStorage(File(".login-sessions"), cached = true)) {
            cookie.path = "/"
            cookie.maxAgeInSeconds = LOGIN_SESSION_MAX_AGE_SECONDS
            cookie.secure = true
            cookie.httpOnly = true
            cookie.extensions["SameSite"] = "None"
        }

        cookie<ContextSession>("Context") {
            cookie.path = "/"
            cookie.secure = true
            cookie.httpOnly = true
            cookie.extensions["SameSite"] = "None"
        }

    }
    monitor.subscribe(ApplicationStopping) {
        oAuthenticationService.destroy()
        println("Session monitor stopping")
    }
}

/**
 * Stores context data that might be needed to accessed my multiple routes, e.g, authentication flow.
 * It is safer and easier than passing the data through the call parameters on every single route.
 * @param loginMethod The login method used to authenticate the user.
 * @see LoginMethod For more info on the login methods.
 */
data class ContextSession(
    val loginMethod: LoginMethod
)
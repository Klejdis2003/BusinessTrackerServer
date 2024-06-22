package com.klejdis.services.plugins

import com.klejdis.services.model.Session
import io.ktor.server.application.*
import io.ktor.server.sessions.*

fun Application.configureSessions() {
    install(Sessions) {
        cookie<Session>("LoginSession") {
            cookie.path = "/"
            cookie.maxAgeInSeconds = 60 * 10 // 10 minutes
            cookie.secure = true
            cookie.httpOnly = true
        }
    }
}
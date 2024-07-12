package com.klejdis.services.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.httpsredirect.*

fun Application.configureHTTPSRedirect() {
    install(HttpsRedirect) {
        sslPort = 8443
        permanentRedirect = true
    }
}
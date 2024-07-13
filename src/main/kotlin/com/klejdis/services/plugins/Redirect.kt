package com.klejdis.services.plugins

import com.klejdis.services.SSL_PORT
import io.ktor.server.application.*
import io.ktor.server.plugins.forwardedheaders.*
import io.ktor.server.plugins.httpsredirect.*

fun Application.configureHTTPSRedirect() {
    install(ForwardedHeaders)
    install(XForwardedHeaders)
    install(HttpsRedirect) {
        sslPort = SSL_PORT
        permanentRedirect = true
    }


}
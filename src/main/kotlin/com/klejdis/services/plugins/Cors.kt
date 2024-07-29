package com.klejdis.services.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

fun Application.configureCORS() {
    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.AccessControlAllowHeaders)
        allowHeader(HttpHeaders.AccessControlAllowOrigin)
        allowHeader(HttpHeaders.Accept)
        allowHeader(HttpHeaders.AcceptLanguage)
        allowHeader(HttpHeaders.ContentLanguage)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.Origin)
        allowHeader(HttpHeaders.UserAgent)
        allowHeader(HttpHeaders.CacheControl)
        allowHeader(HttpHeaders.Pragma)
        allowHeader(HttpHeaders.Cookie)
        allowHeader(HttpHeaders.SetCookie)
        allowHeader(HttpHeaders.Referrer)
        allowHeader(HttpHeaders.AcceptEncoding)
        allowMethod(HttpMethod.Options)
        exposeHeader(HttpHeaders.Location)
        allowCredentials = true
    }
}
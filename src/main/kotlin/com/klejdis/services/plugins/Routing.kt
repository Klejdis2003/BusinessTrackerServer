package com.klejdis.services.plugins

import com.klejdis.services.routes.accountsRoute
import com.klejdis.services.routes.loginRoute
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        authenticate(AuthMethod.Bearer.provider) {
            route("/") {
                get {
                    call.respondText("Hello World!")
                }
            }
        }


        accountsRoute()
        loginRoute()
        // Static plugin. Try to access `/static/index.html`
        staticResources(remotePath = "/static", "static")
    }
}

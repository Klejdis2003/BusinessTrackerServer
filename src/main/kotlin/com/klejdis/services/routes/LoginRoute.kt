package com.klejdis.services.routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.klejdis.services.config.JwtConfig
import com.klejdis.services.model.Account
import com.klejdis.services.services.AccountService
import com.klejdis.services.services.AuthStatus
import com.klejdis.services.services.AuthenticationService
import com.klejdis.services.services.EntityAlreadyExistsException
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.util.*

fun Route.loginRoute() {
    val authService by inject<AuthenticationService>()
    val jwtConfig by inject<JwtConfig>()
    route("/login") {
        post {
            val account = call.receive<Account>()
            val authStatus = authService.authenticate(account.username, account.password)
            when (authStatus) {
                AuthStatus.SUCCESS -> {
                    val token = JWT.create()
                        .withAudience(jwtConfig.audience)
                        .withIssuer(jwtConfig.issuer)
                        .withClaim("username", account.username)
                        .withExpiresAt(Date(System.currentTimeMillis() + 60000))
                        .sign(Algorithm.HMAC256(jwtConfig.secret))
                    call.respond(hashMapOf("token" to token))
                }
                AuthStatus.INVALID_USERNAME -> call.respond(HttpStatusCode.Unauthorized, "Invalid username")
                AuthStatus.INVALID_PASSWORD -> call.respond(HttpStatusCode.Unauthorized, "Invalid password")
            }
        }
    }
}
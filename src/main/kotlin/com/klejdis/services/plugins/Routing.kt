package com.klejdis.services.plugins

import com.klejdis.services.model.Session
import com.klejdis.services.routes.*
import com.klejdis.services.services.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.util.*
import kotlinx.html.body
import kotlinx.html.h1
import kotlinx.html.li
import kotlinx.html.ul
import org.koin.ktor.ext.inject

const val HOME_ROUTE = "/"

fun Application.configureRouting() {
    val oAuthenticationService by inject<OAuthenticationService>()
    val businessService by inject<BusinessService>()
    routing {
        authenticate(AuthMethod.OAuth.provider) {
            accountsRoute()
            authRoute()
            staticResources(remotePath = "/static", "static")
        }

        businessesRoute()
        expenseRoutes()
        ordersRoute()
        get(HOME_ROUTE) {
            val userSession = call.getSession()
            if (userSession != null) {
                val user = oAuthenticationService.getProfileInfoFromToken(userSession.token)
                val items = businessService.getBusinessItems(user.email)
                call.respondHtml {
                    body {
                        h1 { +"Welcome, ${user.email}" }
                        ul {
                            items.forEach {
                                li { +"${it.name} - ${it.price}" }
                            }
                        }
                    }
                }
            }
        }
        get("/login") {
            if (call.sessions.get<Session>() != null)
                call.respondRedirect(HOME_ROUTE)
            else
                call.respondRedirect("/loginRedirect")

        }
        get("/logout") {
            val authToken = call.getSession()?.token
            oAuthenticationService.logout(authToken!!)
            call.sessions.clear<Session>()
        }
    }
}

/**
 * Gets the session from the call. If the session is null, redirects to the login page.
 * It should be used whenever session needs to be accessed, instead of directly calling
 * `call.sessions.get<Session>()`. That is because it features the automatic redirect to the login page,
 * with no need to repeatedly check for the session's existence and knowing what needs to be done.
 * @return The session if it exists, null otherwise.
 */
suspend fun RoutingCall.getSession(): Session? {
    val session: Session? = sessions.get()
    if (session == null) {
        val redirectUrl = url {
            protocol = URLProtocol.HTTPS
            port = 8080
            path("/login")
            parameters.append("redirectUrl", request.uri)
        }
        respondRedirect(redirectUrl)
        return null
    }
    return session

}

suspend fun RoutingCall.handleException(e: Exception) {
    e.printStackTraceIfInDevMode()
    when (e) {
        is EntityNotFoundException -> respond(HttpStatusCode.NotFound, e.message!!)
        is EntityAlreadyExistsException -> respond(HttpStatusCode.Conflict, e.message!!)
        is IllegalArgumentException -> respond(HttpStatusCode.BadRequest, e.message!!)
        is BadRequestException -> {
            val message = e.cause?.message?.substringBefore("for") ?: e.message
            respond(HttpStatusCode.BadRequest, message ?: "Missing required fields.")
        }
        is NoSuchElementException -> respond(HttpStatusCode.BadRequest, e.message!!)
        else -> respond(HttpStatusCode.InternalServerError, "An unexpected error occurred.")
    }
}

suspend fun RoutingCall.executeWithExceptionHandling(block: suspend () -> Unit) {
    try {
        block()
    } catch (e: Exception) {
        handleException(e)
    }
}



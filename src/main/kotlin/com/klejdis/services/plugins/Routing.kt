package com.klejdis.services.plugins

import com.klejdis.services.URL_PORT
import com.klejdis.services.URL_PROTOCOL
import com.klejdis.services.model.Session
import com.klejdis.services.routes.authRoute
import com.klejdis.services.routes.businessesRoute
import com.klejdis.services.routes.expenseRoutes
import com.klejdis.services.routes.ordersRoute
import com.klejdis.services.services.EntityAlreadyExistsException
import com.klejdis.services.services.EntityNotFoundException
import com.klejdis.services.services.printStackTraceIfInDevMode
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.util.*
import java.io.File
import java.time.LocalDateTime

const val HOME_ROUTE = "/orders"

fun Application.configureRouting() {
    routing {
        authRoute()
        businessesRoute()
        expenseRoutes()
        ordersRoute()

        staticFiles(remotePath = "/code_documentation", File("src/main/resources/documentation/code"))
        //openAPI(path = "openapi", swaggerFile = "src/main/resources/openapi/documentation.yaml")
        //swaggerUI(path="swagger", swaggerFile = "src/main/resources/openapi/documentation.yaml")
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
            protocol = URL_PROTOCOL
            port = URL_PORT
            path("/login")
            parameters.append("redirectUrl", request.uri)
        }
        respondRedirect(redirectUrl)
        return null
    }
    val sessionCreationTime = LocalDateTime.parse(session.creationTime)
    val currentTime = LocalDateTime.now()
    if (currentTime > sessionCreationTime.plusSeconds(sessionMaxAgeInSeconds)){
        sessions.clear<Session>()
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



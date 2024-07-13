package com.klejdis.services.plugins

import com.klejdis.services.URL_PORT
import com.klejdis.services.URL_PROTOCOL
import com.klejdis.services.model.Session
import com.klejdis.services.routes.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.util.*
import java.io.File
import java.time.ZonedDateTime

const val HOME_ROUTE = "/orders"

fun Application.configureRouting() {
    routing {
        authRoute()
        businessesRoute()
        expenseRoutes()
        ordersRoute()
        analyticsRoute()

        staticFiles(remotePath = "/code_documentation", File("src/main/resources/documentation/code"))
//        openAPI(path = "openapi", swaggerFile = getResourceFullPath("openapi/documentation.yaml"))
//        swaggerUI(path="swagger", swaggerFile = getResourceFullPath("openapi/documentation.yaml"))
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
    val sessionCreationTime = ZonedDateTime.parse(session.creationTime)
    val currentTime = ZonedDateTime.now()
    if (currentTime > sessionCreationTime.plusSeconds(sessionMaxAgeInSeconds)){
        sessions.clear<Session>()
        return null
    }

    return session

}







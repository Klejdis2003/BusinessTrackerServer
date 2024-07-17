package com.klejdis.services.plugins

import com.klejdis.services.URL_PORT
import com.klejdis.services.URL_PROTOCOL
import com.klejdis.services.model.Session
import com.klejdis.services.routes.*
import com.klejdis.services.util.FileOperations
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.util.*
import java.io.File
import java.time.ZonedDateTime

const val HOME_ROUTE = "/orders"
const val DEFAULT_IMAGES_ENDPOINT = "/images"

fun Application.configureRouting() {
    routing {
        authRoute()
        businessesRoute()
        expenseRoutes()
        ordersRoute()
        analyticsRoute()
        itemsRoute()
        currencyRoutes()
        staticFiles(remotePath = "/code_documentation", File("src/main/resources/documentation/code"))
        staticFiles(remotePath = DEFAULT_IMAGES_ENDPOINT, File("uploads/images"))
        openAPI(path = "openapi", swaggerFile = FileOperations.getResourceAsRelativePath("openapi/documentation.yaml"))
        swaggerUI(path="swagger", swaggerFile = FileOperations.getResourceAsRelativePath("openapi/documentation.yaml"))
    }
}

/**
 * Gets the session from the call. If the session is null, redirects to the login page.
 * It should be used whenever session needs to be accessed, instead of directly calling
 * `call.sessions.get<Session>()`. That is because it features the automatic redirect to the login page,
 * with no need to repeatedly check for the session's existence and knowing what needs to be done.
 * @return The session if it exists, null otherwise.
 */
suspend fun ApplicationCall.getSession(): Session? {
    val session: Session? = sessions.get()
    if (session == null) {
        val redirectUrl = url {
            protocol = URL_PROTOCOL
            port = URL_PORT
            path("/login")
            if(request.uri != "/logout") //avoid hitting back the logout endpoint again and again
                parameters.append("redirectUrl", request.uri)
        }
        respondRedirect(redirectUrl)
        return null
    }
    val sessionCreationTime = ZonedDateTime.parse(session.creationTime)
    val currentTime = ZonedDateTime.now()
    if (currentTime > sessionCreationTime.plusSeconds(SESSION_MAX_AGE_SECONDS)){
        sessions.clear<Session>()
        return null
    }

    return session

}







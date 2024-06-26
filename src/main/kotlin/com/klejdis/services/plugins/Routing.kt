package com.klejdis.services.plugins

import com.klejdis.services.model.Session
import com.klejdis.services.routes.accountsRoute
import com.klejdis.services.routes.authRoute
import com.klejdis.services.routes.businessesRoute
import com.klejdis.services.routes.ordersRoute
import com.klejdis.services.services.BusinessService
import com.klejdis.services.services.OAuthenticationService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
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
        ordersRoute()
        get(HOME_ROUTE) {
            val userSession = call.getSession()
            if(userSession != null) {
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
            if(call.sessions.get<Session>() != null)
                call.respondRedirect(HOME_ROUTE)
            else
                call.respondRedirect("/loginRedirect")

        }
        get ("/logout"){
            val authToken = call.getSession()?.token
            oAuthenticationService.logout(authToken!!)
            call.sessions.clear<Session>()
        }
    }
}

/**
 * Gets the session from the call. If the session is null, redirects to the login page.
 * @return The session if it exists, null otherwise.
 */
suspend fun RoutingCall.getSession(): Session? {
    val session: Session? = sessions.get()
    if(session == null) {
        val redirectUrl = url{
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

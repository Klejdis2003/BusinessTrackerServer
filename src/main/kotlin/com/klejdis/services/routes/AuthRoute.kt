package com.klejdis.services.routes

import com.klejdis.services.model.ProfileInfo
import com.klejdis.services.model.Session
import com.klejdis.services.plugins.HOME_ROUTE
import com.klejdis.services.plugins.getSession
import com.klejdis.services.plugins.redirects
import com.klejdis.services.services.OAuthenticationService
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import org.koin.ktor.ext.inject
import org.koin.ktor.ext.get as koinGet

fun Route.authRoute() {
    val authenticationService = koinGet<OAuthenticationService>()

    get("/login") {
        if (call.sessions.get<Session>() != null)
            call.respondRedirect(HOME_ROUTE)
        else
            call.respondRedirect("/loginRedirect")

    }
    get("/logout") {
        val authToken = call.getSession()?.token
        authenticationService.logout(authToken!!)
        call.sessions.clear<Session>()
    }

    get("/loginRedirect") {
        //Ktor automatically redirects to callback URL
    }

    route("/callback") {
        get {
            val currentPrincipal: OAuthAccessTokenResponse.OAuth2? = call.principal()
            currentPrincipal?.let { principal ->
                principal.state?.let { state ->
                    authenticationService.login(principal)
                    call.sessions.set(Session(generateSessionId(), principal.accessToken))
                    redirects[state]?.let { redirectUrl ->
                        call.respondRedirect(redirectUrl)
                        return@get
                    }
                }
            }
            call.respondRedirect(HOME_ROUTE)
        }
    }
}


suspend fun RoutingCall.getProfileInfoFromSession(): ProfileInfo? {
    val authenticationService by inject<OAuthenticationService>()
    val session = getSession()
    return session?.let { authenticationService.getProfileInfoFromToken(it.token) }
}
package com.klejdis.services.routes

import com.klejdis.services.model.ProfileInfo
import com.klejdis.services.model.Session
import com.klejdis.services.plugins.HOME_ROUTE
import com.klejdis.services.plugins.redirects
import com.klejdis.services.services.AuthenticationService
import com.klejdis.services.services.OAuthenticationService
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import org.koin.ktor.ext.inject

fun Route.authRoute() {
    val authenticationService by inject<OAuthenticationService>()
    get("/loginRedirect") {
        //Ktor automatically redirects to callback URL
    }

    route("/callback"){
        get{
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
    val session = sessions.get<Session>()
    return session?.let { authenticationService.getProfileInfoFromToken(it.token) }
}
package com.klejdis.services.routes

import com.klejdis.services.model.ProfileInfo
import com.klejdis.services.model.Session
import com.klejdis.services.plugins.*
import com.klejdis.services.printIfDebugMode
import com.klejdis.services.services.OAuthenticationService
import com.klejdis.services.services.UnauthorizedException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.util.*
import org.koin.core.parameter.parametersOf
import org.koin.ktor.ext.inject
import org.koin.mp.KoinPlatform.getKoin
import org.koin.ktor.ext.get as koinGet

/**
 * The route that handles the authentication of the user. It uses the OAuth2 protocol to authenticate the user.
 * The route has the following endpoints:
 * - /loginRedirect: Redirects the user to the OAuth provider's login page.
 * - /callback: The OAuth provider redirects the user to this endpoint after the user has logged in. The endpoint
 * fetches the user's profile info and stores it in the session.
 * - /login: Redirects the user to the OAuth provider's login page. If the user is already logged in, it redirects to the home page.
 * - /logout: Logs the user out and redirects to the OAuth provider's logout page.
 * @see getProfileInfoFromSession For more info on how the profile info is fetched
 * @see getScopedService For more info on how the service is fetched
 */
fun Route.authRoute() {
    val authenticationService = koinGet<OAuthenticationService>()

    authenticate(AuthMethod.OAuth.provider) {
        get("/loginRedirect") {
            //Ktor automatically redirects to callback URL
        }

        get("/callback") {
            val currentPrincipal: OAuthAccessTokenResponse.OAuth2? = call.principal()
            currentPrincipal?.let { principal ->
                principal.state?.let { state ->
                    println(principal.expiresIn)
                    call.sessions.set(Session(generateSessionId(), principal.accessToken))
                    printIfDebugMode("Session ${call.getSession()} created.")
                    authenticationService.login(principal)
                    redirects[state]?.let { redirectUrl ->
                        call.respondRedirect(redirectUrl)
                        return@get
                    }
                }
            }
            call.respondRedirect(HOME_ROUTE)
        }
    }

    get("/login") {
        if (call.sessions.get<Session>() != null)
            call.respondRedirect(HOME_ROUTE)
        else
            call.respondRedirect("/loginRedirect?redirectUrl=${call.parameters["redirectUrl"] ?: HOME_ROUTE}")

    }
    get("/logout") {
        //no prefetching
        call.response.header("Cache-Control", "no-cache, no-store, must-revalidate")
        call.response.header("X-Robots-Tag", "noindex, nofollow, noarchive, nosnippet, noodp, notranslate, noimageindex")
        val authToken = call.getSession()?.token
        call.sessions.clear<Session>()
        authToken?.let { authenticationService.logout(it) }
        call.respondRedirect(getLogoutRequestUrl())
    }

}

private fun getLogoutRequestUrl(): String {
    val url =
        url {
            protocol = URLProtocol.HTTPS
            host = OAUTH_DOMAIN
            path("v2", "logout")
            parameters.append("client_id", System.getenv("AUTH0_CLIENT_ID"))
            parameters.append("returnTo", "${System.getenv("APPLICATION_DOMAIN")}/login")
        }
    return url
}

/**
 * Uses the current session data to fetch the profile info of the logged in business user from the OAuth provider or local cache.
 * The [getSession] method is used to retrieve the session data, so if the session is null, it is up to it to decide what happens.
 * @return ProfileInfo object if the user is logged in, null otherwise.
 * @see getSession For more info on how the session is retrieved
 * @see OAuthenticationService.getProfileInfoFromToken For more info on how the profile info is fetched
 */
suspend fun ApplicationCall.getProfileInfoFromSession(): ProfileInfo? {
    val authenticationService by inject<OAuthenticationService>()
    val session = getSession()
    return session?.let {
        try {
            authenticationService.getProfileInfoFromToken(it.token) {
                sessions.clear<Session>()
            }
        }
        catch (e: Exception) { null }
    }
}

/**
 * Gets the requested service from the current session scope. If the session is not found, an exception is thrown.
 * Additionally, the session is received through the call's [getProfileInfoFromSession] method. It redirects to the login page if the
 * session is not found.
 * @return The requested service
 * @throws Exception if the session is not found
 * @see getProfileInfoFromSession
 */
suspend inline fun<reified T> ApplicationCall.getScopedService(): T {
    val loggedInEmail = getProfileInfoFromSession()?.email ?: throw UnauthorizedException("User is not logged in.")
    return getKoin().getOrCreateScope<Session>(loggedInEmail).get { parametersOf(loggedInEmail) }
}

inline fun<reified T> getScopedService(loggedInEmail: String): T {
    val scope = getKoin().getOrCreateScope<Session>(loggedInEmail)
    return scope.get { parametersOf(loggedInEmail) }
}
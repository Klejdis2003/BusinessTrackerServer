package com.klejdis.services.routes

import com.klejdis.services.extensions.getScopedService
import com.klejdis.services.extensions.getSession
import com.klejdis.services.model.LoginSession
import com.klejdis.services.model.ProfileInfo
import com.klejdis.services.plugins.AuthMethod
import com.klejdis.services.plugins.ContextSession
import com.klejdis.services.plugins.OAUTH_DOMAIN
import com.klejdis.services.plugins.redirects
import com.klejdis.services.printIfDebugMode
import com.klejdis.services.services.OAuthenticationService
import com.klejdis.services.storage.InMemoryLoginSessionStorage
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.util.*
import io.ktor.util.*
import org.koin.ktor.ext.inject
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
 * @see ApplicationCall.getScopedService For more info on how the service is fetched
 */
fun Route.authRoute() {
    val authenticationService = koinGet<OAuthenticationService>()
    val sessionStorage = koinGet<InMemoryLoginSessionStorage>()
    authenticate(AuthMethod.OAuth.provider) {
        get("/loginRedirect") {
            //Ktor automatically redirects to callback URL
        }

        get("/callback") {
            val currentPrincipal: OAuthAccessTokenResponse.OAuth2? = call.principal()
            val loginMethod = call.sessions.get<ContextSession>()?.loginMethod ?: call.respond("No login method provided.")
            call.sessions.clear<ContextSession>()
            currentPrincipal?.let { principal ->
                principal.state?.let { state ->
                    val session = LoginSession(generateSessionId(),  principal.accessToken)
                    if(loginMethod == LoginMethod.Session) {
                        call.sessions.set(session)
                        printIfDebugMode("Session ${call.getSession()} created.")
                    }
                    else {
                        sessionStorage.write(session.id, session.token)
                    }
                    authenticationService.login(principal)
                    redirects[state]?.let { redirectUrl ->
                        call.respondRedirect("$redirectUrl?sessionId=${session.id.encodeBase64()}")
                        return@get
                    }
                }
            }
        }
    }

    get("/login") {
        val loginMethod =
            call.parameters[LoginMethod.URL_PARAM]?.let {
                LoginMethod.valueOf(it, false) } ?: LoginMethod.Session

        val redirectUrl = call.parameters["redirectUrl"]
        if(loginMethod == LoginMethod.Token)
            redirectUrl ?: call.respond("For token retrieval, a redirect url MUST be provided.")

        if (call.sessions.get<LoginSession>() != null || call.request.headers[HttpHeaders.Authorization] != null)
            call.respondRedirect(redirectUrl ?: "/")
        else {
            call.sessions.set(ContextSession(loginMethod))
            call.respondRedirect("/loginRedirect?redirectUrl=$redirectUrl")
        }

    }
    get("/logout") {
        //no prefetching
        call.response.header("Cache-Control", "no-cache, no-store, must-revalidate")
        call.response.header("X-Robots-Tag", "noindex, nofollow, noarchive, nosnippet, noodp, notranslate, noimageindex")
        val authToken = call.getSession()?.token
        call.sessions.clear<LoginSession>()
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
                sessions.clear<LoginSession>()
            }
        }
        catch (e: Exception) { null }
    }
}

enum class LoginMethod {
    Session,
    Token;
    companion object {
        const val URL_PARAM = "method"
        fun valueOf(value: String, isCaseSensitive : Boolean = false): LoginMethod {
            if(isCaseSensitive) return valueOf(value)
            return LoginMethod.valueOf(value.lowercase().replaceFirstChar { it.uppercase() })
        }
    }
}


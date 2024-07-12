package com.klejdis.services.plugins

import io.ktor.client.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import org.koin.ktor.ext.inject

val redirects = mutableMapOf<String, String>()
const val OAUTH_DOMAIN = "dev-ff32y82lak8hyod4.us.auth0.com"
fun Application.configureSecurity() {
    val environment = System.getenv()
    val httpClient by inject<HttpClient>()
    install(Authentication) {
        oauth(AuthMethod.OAuth.provider) {
            val oAuthServerSettings =
                //OIDC Conformant
                OAuthServerSettings.OAuth2ServerSettings(
                    name = "ktor-oauth",
                    authorizeUrl = "https://$OAUTH_DOMAIN/authorize",
                    accessTokenUrl = "https://$OAUTH_DOMAIN/oauth/token",
                    requestMethod = HttpMethod.Post,
                    clientId = environment["AUTH0_CLIENT_ID"]!!,
                    clientSecret = environment["AUTH0_CLIENT_SECRET"]!!,
                    defaultScopes = listOf("profile", "openid", "email", "offline_access"),
                    extraAuthParameters = listOf("audience" to environment["AUTH0_AUDIENCE"]!!),
                    onStateCreated = { call, state ->
                        call.request.queryParameters["redirectUrl"]?.let {
                            redirects[state] = it
                        }
                    }
                )

            urlProvider = { System.getenv("AUTH0_CALLBACK_URL") ?: "https://localhost:8443/callback" }
            providerLookup = { oAuthServerSettings }
            client = httpClient
        }
    }


}

sealed class AuthMethod(val provider: String) {
    data object OAuth : AuthMethod("auth0-okta")
}
package com.klejdis.services.plugins

import io.github.cdimascio.dotenv.Dotenv
import io.ktor.client.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import org.koin.ktor.ext.inject

val redirects = mutableMapOf<String, String>()
const val OAUTH_DOMAIN = "dev-ff32y82lak8hyod4.us.auth0.com"
fun Application.configureSecurity() {
    val vault by inject<Dotenv>()
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
                    clientId = vault["AUTH0_CLIENT_ID"],
                    clientSecret = vault["AUTH0_CLIENT_SECRET"],
                    defaultScopes = listOf("profile", "openid", "email", "offline_access"),
                    extraAuthParameters = listOf("audience" to vault["AUTH0_AUDIENCE"]),
                    onStateCreated = { call, state ->
                        call.request.queryParameters["redirectUrl"]?.let {
                            redirects[state] = it
                        }
                    }
                )

            urlProvider = { "https://localhost:8080/callback" }
            providerLookup = { oAuthServerSettings }
            client = httpClient
        }
    }


}

sealed class AuthMethod(val provider: String) {
    data object OAuth : AuthMethod("auth0-okta")
}
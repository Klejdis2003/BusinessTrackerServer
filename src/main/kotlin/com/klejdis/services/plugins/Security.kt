package com.klejdis.services.plugins

import com.klejdis.services.config.JwtConfig
import io.github.cdimascio.dotenv.Dotenv
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import org.dotenv.vault.DotenvVault
import org.dotenv.vault.dotenvVault
import org.koin.java.KoinJavaComponent.inject
import org.koin.ktor.ext.inject


fun Application.configureSecurity() {
    val vault by inject<Dotenv>()
    val jwtConfig by inject<JwtConfig>()
    install(Authentication) {
        jwt(AuthMethod.JWT.provider) {
            realm = jwtConfig.realm
        }
        bearer(AuthMethod.Bearer.provider){
            realm = "ktor"
            authenticate { bearerTokenCredential ->
                if (bearerTokenCredential.token == vault["CONST_BEARER_TOKEN"]) {
                    UserIdPrincipal("user")
                } else {
                    null
                }
            }
        }
    }
}



sealed class AuthMethod(val provider: String) {
    data object Bearer : AuthMethod("auth-bearer")
    data object Digest : AuthMethod("auth-digest")
    data object Basic : AuthMethod("auth-basic")
    data object Form : AuthMethod("auth-form")
    data object JWT : AuthMethod("auth-jwt")
    data object Session : AuthMethod("auth-session")
}
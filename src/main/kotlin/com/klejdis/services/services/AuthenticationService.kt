package com.klejdis.services.services

import com.klejdis.services.model.ProfileInfo
import io.ktor.server.auth.*


/**
 * Interface for handling the authentication process.
 * @param T The type of the user's profile information.
 */
interface AuthenticationService<in K, out T> {
    /**
     * Logs the user in using the token received from the Auth provider.
     * @param token The token received from the Auth provider.
     * @return Defined by the implementation.
     */
    suspend fun login(tokenResponse: K): T

    /**
     * Logs the user out by clearing the session.
     * @param token The token received from the Auth provider.
     * @return Defined by the implementation.
     */
    suspend fun logout(authToken: String): Boolean


    /**
     * Uses the received token to grab the user's profile information.
     * @param token The token received from Auth provider.
     * @return Defined by the implementation.
     */
    suspend fun getProfileInfoFromToken(token: String): T
}

typealias OAuth2Response = OAuthAccessTokenResponse.OAuth2
interface OAuthenticationService: AuthenticationService<OAuth2Response, ProfileInfo>
package com.klejdis.services.services

import com.klejdis.services.endKoinBusinessScope
import com.klejdis.services.getScopedService
import com.klejdis.services.model.ProfileInfo
import com.klejdis.services.plugins.LOGIN_SESSION_MAX_AGE_SECONDS
import com.klejdis.services.plugins.OAUTH_DOMAIN
import com.klejdis.services.printIfDebugMode
import com.klejdis.services.request.OAuthTokenRevocationRequest
import com.klejdis.services.startKoinBusinessScope
import com.klejdis.services.util.BackgroundTasksUtil
import com.klejdis.services.util.getZonedDateTimeNow
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.util.*
import kotlinx.coroutines.Job
import java.time.ZonedDateTime
import java.util.concurrent.ConcurrentHashMap


class OAuthenticationServiceImpl(
    private val httpClient: HttpClient
) : OAuthenticationService {
    private val tokenProfileInfoMap: MutableMap<String, ProfileInfo> = ConcurrentHashMap()
    private val authTokenToTokenResponseMap: MutableMap<String, OAuth2Response> = ConcurrentHashMap()
    private val authTokenToCreationTimeMap: MutableMap<String, ZonedDateTime> = ConcurrentHashMap()
    private val job = invalidateExpiredSessions() //storing it to ensure it is properly terminated when the service is destroyed

    init{
        job.start()
    }

    /**
     * Gets the profile information from the token.
     * @param token The token received from the Auth provider.
     * @return The user's profile information.
     */
    override suspend fun getProfileInfoFromToken(
        token: String,
        unauthorizedFailure: suspend () -> Unit
    ): ProfileInfo? {
        if (tokenProfileInfoMap.containsKey(token)) {
            println("Profile Info CACHE HIT")
            return tokenProfileInfoMap[token]!!
        }

        println("Making request to get profile info from token $token")
        val url = url {
            protocol = URLProtocol.HTTPS
            host = OAUTH_DOMAIN
            path("userinfo")
        }
        val header = headers { append(HttpHeaders.Authorization, "Bearer $token") }
        val response = httpClient.get(url) {
            headers.appendAll(header)
        }
        if(response.status == HttpStatusCode.Unauthorized) {
            unauthorizedFailure()
            return null
        }

        val profileInfo: ProfileInfo = response.body()
        tokenProfileInfoMap[token] = profileInfo
        return profileInfo
    }

    override fun destroy() {
        job.cancel()
    }

    /**
     * Logs the user in using the token received from the Auth provider.
     * Creates a new user if they do not exist.
     * @param tokenResponse The token received from the Auth provider.
     * @return The user's profile information.
     */
    override suspend fun login(tokenResponse: OAuth2Response, onSuccessfulLogin: suspend (ProfileInfo) -> Unit): ProfileInfo {
        val profileInfo = getProfileInfoFromToken(tokenResponse.accessToken) ?: throw Exception("Could not get profile info.")
        cacheData(tokenResponse, profileInfo)
        startKoinBusinessScope(profileInfo.email)
        val businessService = getScopedService<BusinessService>(profileInfo.email)
        businessService.createIfNotExists(profileInfo.email)
        onSuccessfulLogin(profileInfo)
        return profileInfo
    }

    override suspend fun logout(authToken: String, onSuccessfulLogout: suspend () -> Unit): Boolean {
        val tokenResponse = authTokenToTokenResponseMap[authToken]
        val email = getProfileInfoFromToken(authToken)?.email ?: return false
        println("Logging out user $email.")
        endKoinBusinessScope(email)
        if (tokenResponse?.refreshToken == null) {
            println("Logout request with no refresh token. Ignoring.")
            return false
        }

        val refreshToken = tokenResponse.refreshToken!!
        val tokenRevocationResponseSuccessful = makeRevocationRequest(refreshToken)
        removeFromCache(tokenResponse.accessToken)
        onSuccessfulLogout()
        return tokenRevocationResponseSuccessful
    }

    private suspend fun makeRevocationRequest(refreshToken: String): Boolean {
        val response = httpClient.post {
            url {
                protocol = URLProtocol.HTTPS
                host = OAUTH_DOMAIN
                path("oauth", "revoke")
            }
            contentType(ContentType.Application.Json)
            setBody(
                OAuthTokenRevocationRequest(
                    clientId = System.getenv("AUTH0_CLIENT_ID"),
                    clientSecret = System.getenv("AUTH0_CLIENT_SECRET"),
                    refreshToken = refreshToken
                )
            )
        }
        return response.status == HttpStatusCode.OK
    }


    private fun cacheData(tokenResponse: OAuth2Response, profileInfo: ProfileInfo) {
        tokenProfileInfoMap.putIfAbsent(tokenResponse.accessToken, profileInfo)
        authTokenToTokenResponseMap.putIfAbsent(tokenResponse.accessToken, tokenResponse)
        authTokenToCreationTimeMap.putIfAbsent(tokenResponse.accessToken, getZonedDateTimeNow())
    }

    private fun removeFromCache(token: String) {
        tokenProfileInfoMap.remove(token)
        authTokenToTokenResponseMap.remove(token)
        authTokenToCreationTimeMap.remove(token)
    }

    /**
     * Runs every minute and checks the cache for expired tokens. If a token is expired, it is removed from the cache.
     */
    private fun invalidateExpiredSessions(): Job {
        return BackgroundTasksUtil.run(60) {
            println("Checking for expired tokens...")
            printIfDebugMode("Current tokens: $authTokenToCreationTimeMap")
            authTokenToCreationTimeMap.forEach { (token, creationTime) ->
                if (getZonedDateTimeNow() > creationTime.plusSeconds(LOGIN_SESSION_MAX_AGE_SECONDS)) {
                    println("Removing expired token for user ${tokenProfileInfoMap[token]?.email}")
                    logout(token)
                    removeFromCache(token)
                }
            }
        }
    }

}


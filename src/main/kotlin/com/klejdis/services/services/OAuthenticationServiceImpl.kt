package com.klejdis.services.services

import com.klejdis.services.model.ProfileInfo
import com.klejdis.services.plugins.OAUTH_DOMAIN
import com.klejdis.services.plugins.sessionMaxAgeInSeconds
import com.klejdis.services.request.OAuthTokenRevocationRequest
import com.klejdis.services.util.BackgroundTasksUtil
import io.github.cdimascio.dotenv.Dotenv
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.util.*
import kotlinx.coroutines.Job
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap


class OAuthenticationServiceImpl(
    private val httpClient: HttpClient,
    private val businessService: BusinessService,
    private val vault: Dotenv
) : OAuthenticationService {
    private val tokenProfileInfoMap: MutableMap<String, ProfileInfo> = ConcurrentHashMap()
    private val authTokenToTokenResponseMap: MutableMap<String, OAuth2Response> = ConcurrentHashMap()
    private val authTokenToCreationTimeMap: MutableMap<String, LocalDateTime> = ConcurrentHashMap()
    private val job = invalidateExpiredSessions()

    init{
        job.start()
    }

    /**
     * Gets the profile information from the token.
     * @param token The token received from the Auth provider.
     * @return The user's profile information.
     */
    override suspend fun getProfileInfoFromToken(token: String): ProfileInfo {

        if (tokenProfileInfoMap.containsKey(token)) {
            println("Profile Info CACHE HIT")
            return tokenProfileInfoMap[token]!!
        }

        val url = url {
            protocol = URLProtocol.HTTPS
            host = "dev-ff32y82lak8hyod4.us.auth0.com"
            path("userinfo")
        }
        val header = headers { append(HttpHeaders.Authorization, "Bearer $token") }
        val response = httpClient.get(url) {
            headers.appendAll(header)
        }
        val profileInfo: ProfileInfo = response.body()
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
    override suspend fun login(tokenResponse: OAuth2Response): ProfileInfo {
        val profileInfo = getProfileInfoFromToken(tokenResponse.accessToken)
        cacheData(tokenResponse, profileInfo)
        businessService.createIfNotExists(profileInfo.email)
        return profileInfo
    }

    override suspend fun logout(authToken: String): Boolean {
        val tokenResponse = authTokenToTokenResponseMap[authToken]
        if (tokenResponse?.refreshToken == null) {
            println("Logout request with no refresh token. Ignoring.")
            return false
        }

        val refreshToken = tokenResponse.refreshToken!!
        val tokenRevocationResponseSuccessful = makeRevocationRequest(refreshToken)
        removeFromCache(tokenResponse.accessToken)

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
                    clientId = vault["AUTH0_CLIENT_ID"],
                    clientSecret = vault["AUTH0_CLIENT_SECRET"],
                    refreshToken = refreshToken
                )
            )
        }
        return response.status == HttpStatusCode.OK
    }

    private fun cacheData(tokenResponse: OAuth2Response, profileInfo: ProfileInfo) {
        tokenProfileInfoMap[tokenResponse.accessToken] = profileInfo
        authTokenToTokenResponseMap[tokenResponse.accessToken] = tokenResponse
        authTokenToCreationTimeMap[tokenResponse.accessToken] = LocalDateTime.now()
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
            authTokenToCreationTimeMap.forEach { (token, creationTime) ->
                if (LocalDateTime.now() > creationTime.plusSeconds(sessionMaxAgeInSeconds)) {
                    println("Removing expired token: $token for user ${tokenProfileInfoMap[token]?.email}")
                    logout(token)
                    removeFromCache(token)
                }
            }
        }
    }

}


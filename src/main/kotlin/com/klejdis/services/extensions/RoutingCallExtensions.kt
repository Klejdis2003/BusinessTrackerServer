package com.klejdis.services.extensions

import com.klejdis.services.model.LoginSession
import com.klejdis.services.model.ProfileInfo
import com.klejdis.services.plugins.LOGIN_SESSION_MAX_AGE_SECONDS
import com.klejdis.services.routes.getProfileInfoFromSession
import com.klejdis.services.services.*
import com.klejdis.services.storage.InMemoryLoginSessionStorage
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import org.koin.ktor.ext.get
import java.time.ZonedDateTime


/**
 * Gets the session from the call. If the session is null, redirects to the login page.
 * It should be used whenever session needs to be accessed, instead of directly calling
 * `call.sessions.get<Session>()`. That is because it features the automatic redirect to the login page,
 * with no need to repeatedly check for the session's existence and knowing what needs to be done.
 * @return The session if it exists, null otherwise.
 */
fun ApplicationCall.getSession(): LoginSession? {
    val loginSession: LoginSession = sessions.get() ?: return null
    val sessionCreationTime = ZonedDateTime.parse(loginSession.creationTime)
    val currentTime = ZonedDateTime.now()
    if (currentTime > sessionCreationTime.plusSeconds(LOGIN_SESSION_MAX_AGE_SECONDS)){
        sessions.clear<LoginSession>()
        return null
    }

    return loginSession

}

suspend fun ApplicationCall.getProfileInfoFromHeaderToken(): ProfileInfo? {
    val authenticationService = get<OAuthenticationService>()
    val sessionId = request.headers["Authorization"] ?: return null
    val token =
        try { InMemoryLoginSessionStorage.read(sessionId) }
        catch (e: NoSuchElementException) { return null }

    return authenticationService.getProfileInfoFromToken(token)
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
    val loggedInEmail =
        getProfileInfoFromSession()?.email
        ?: getProfileInfoFromHeaderToken()?.email
        ?: throw UnauthorizedException("No session in store or token in header.")
    return com.klejdis.services.getScopedService(loggedInEmail)
}


/**
 * Handles the given exception by responding with the appropriate status code and message.
 * @param e The exception to handle.
 */
suspend fun ApplicationCall.handleException(e: Exception) {
    e.printStackTraceIfInDevMode()
    when (e) {
        is EntityNotFoundException -> respond(HttpStatusCode.NotFound, e.message!!)
        is EntityAlreadyExistsException -> respond(HttpStatusCode.Conflict, e.message!!)
        is UnauthorizedException -> respond(HttpStatusCode.Unauthorized, e.message?: "Unauthorized.")

        is IllegalArgumentException -> respond(HttpStatusCode.BadRequest, e.message!!)
        is BadRequestException -> {
            val message = e.cause?.message?.substringBefore("for") ?: e.message
            respond(HttpStatusCode.BadRequest, message ?: "Missing required fields.")
        }
        is NoSuchElementException -> respond(HttpStatusCode.BadRequest, e.message!!)
        else -> respond(HttpStatusCode.InternalServerError, "An unexpected error occurred.")
    }
}

/**
 * Executes the given block and handles any exceptions that occur during the execution.
 * @param block The block to execute.
 */
suspend fun ApplicationCall.executeWithExceptionHandling(block: suspend (ApplicationCall) -> Unit) {
    try {
        block(this)
    } catch (e: Exception) {
        handleException(e)
    }
}

/**
 * Responds with the given message and handles any exceptions that occur during the response.
 * @param message The message to respond with.
 */
suspend inline fun <reified T: Any> ApplicationCall.respondWithExceptionHandling(message: T) {
    executeWithExceptionHandling {
        respond(message)
    }
}
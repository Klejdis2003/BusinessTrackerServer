package com.klejdis.services.extensions

import com.klejdis.services.services.EntityAlreadyExistsException
import com.klejdis.services.services.EntityNotFoundException
import com.klejdis.services.services.UnauthorizedException
import com.klejdis.services.services.printStackTraceIfInDevMode
import io.ktor.http.*
import io.ktor.server.plugins.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Handles the given exception by responding with the appropriate status code and message.
 * @param e The exception to handle.
 */
suspend fun RoutingCall.handleException(e: Exception) {
    e.printStackTraceIfInDevMode()
    when (e) {
        is EntityNotFoundException -> respond(HttpStatusCode.NotFound, e.message!!)
        is EntityAlreadyExistsException -> respond(HttpStatusCode.Conflict, e.message!!)
        is UnauthorizedException -> respond(HttpStatusCode.Unauthorized, e.message!!)
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
suspend fun RoutingCall.executeWithExceptionHandling(block: suspend (RoutingCall) -> Unit) {
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
suspend inline fun <reified T: Any> RoutingCall.respondWithExceptionHandling(message: T) {
    return executeWithExceptionHandling {
        respond(message)
    }
}
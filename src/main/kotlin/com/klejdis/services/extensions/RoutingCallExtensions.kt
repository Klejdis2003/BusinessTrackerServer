package com.klejdis.services.extensions

import com.klejdis.services.services.EntityAlreadyExistsException
import com.klejdis.services.services.EntityNotFoundException
import com.klejdis.services.services.UnauthorizedException
import com.klejdis.services.services.printStackTraceIfInDevMode
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*

/**
 * Handles the given exception by responding with the appropriate status code and message.
 * @param e The exception to handle.
 */
suspend fun ApplicationCall.handleException(e: Exception) {
    e.printStackTraceIfInDevMode()
    when (e) {
        is EntityNotFoundException -> respond(HttpStatusCode.NotFound, e.message!!)
        is EntityAlreadyExistsException -> respond(HttpStatusCode.Conflict, e.message!!)
        is UnauthorizedException -> {
            respondRedirect("/login?redirect=${request.uri}")
        }
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
    return executeWithExceptionHandling {
        respond(message)
    }
}
package com.klejdis.services.services

import com.klejdis.services.MODE
import com.klejdis.services.Mode

class EntityNotFoundException(
    message: String = "The requested entity was not found."
) : Exception(message)

class EntityAlreadyExistsException(
    message: String = "Entity already exists. Cannot create a duplicate."
) : Exception(message)

class InvalidUsernameException(
    message: String = "Invalid username."
) : Exception(message)

class UnauthorizedException(message: String = "You are not authorized to perform this action") : Exception()

fun Exception.printStackTraceIfInDevMode() {
    if (MODE == Mode.DEV) printStackTrace()
}
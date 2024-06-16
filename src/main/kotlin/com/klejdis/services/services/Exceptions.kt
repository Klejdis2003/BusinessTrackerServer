package com.klejdis.services.services

class EntityNotFoundException(
    message: String = "The requested entity was not found."
) : Exception(message)

class EntityAlreadyExistsException(
    message: String = "Entity already exists. Cannot create a duplicate."
) : Exception(message)

class InvalidUserameException(
    message: String = "Invalid username."
) : Exception(message)
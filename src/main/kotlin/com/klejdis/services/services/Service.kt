package com.klejdis.services.services

import org.ktorm.entity.Entity
import org.postgresql.util.PSQLException
import org.postgresql.util.PSQLState
import kotlin.jvm.Throws

/**
 * A general purpose service class for handling basic CRUD operations and exceptions.
 * Can be extended to provide more specific functionality.
 */
open class Service<T: Entity<T>>(
    private val entityName : String
) {
    /**
     * Handles Postgres exceptions and returns a more user-friendly error message.
     * For more specific error messages and handling, it can be overridden in a subclass.
     * @param e The Postgres exception to handle.
     * @return The user-friendly exception.
     */
    open fun handlePSQLException(e: PSQLException): Exception {
        e.printStackTraceIfInDevMode()
        return when (e.sqlState) {
            PSQLState.UNIQUE_VIOLATION.state -> EntityAlreadyExistsException("$entityName already exists. Cannot create a duplicate.")
            PSQLState.NOT_NULL_VIOLATION.state -> IllegalArgumentException("Invalid input. Required fields are missing.")
            PSQLState.CHECK_VIOLATION.state -> IllegalArgumentException("Invalid input. Check constraints failed.")
            else -> e
        }
    }

    /**
     * Pass the code to create a new entity as a lambda. Error handling is done automatically through
     * the [handlePSQLException] method. If you want to customize error handling, override that method.
     * @param block The code to create a new entity.
     * @return The created entity.
     * @throws EntityAlreadyExistsException If the entity already exists in the database.
     * @throws IllegalArgumentException If the input is invalid.
     * @throws PSQLException If a general database error occurs.
     * @throws Exception If an unknown error occurs.
     */
    suspend fun executeCreateBlockWithErrorHandling (block: suspend () -> T): T {
        return try {
            block()
        } catch (e: PSQLException) {
            throw handlePSQLException(e)
        }
        catch (e: Exception) {
            e.printStackTrace()
            throw Exception("An unknown error occurred.")
        }

    }

    @Throws(EntityNotFoundException::class)
    /**
     * Pass the code to update an entity as a lambda. Error handling is done automatically.
     * @param block The code to update an entity.
     * @return The updated entity
     * @throws EntityNotFoundException If the entity does not exist in the database.
     * @throws IllegalArgumentException If the input is invalid.
     * @throws PSQLException If a general database error occurs.
     * @throws Exception If an unknown error occurs.
     */
    suspend fun executeUpdateBlock (block: suspend () -> T): T {
        return try {
            block()
        } catch (e: PSQLException) {
            throw handlePSQLException(e)
        } catch (e: IllegalArgumentException) {
            throw e
        }
    }

}
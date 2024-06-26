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
    fun handlePSQLException(e: PSQLException, message: String = ""): Exception {
        println(e.sqlState)
        return when (e.sqlState) {
            PSQLState.UNIQUE_VIOLATION.state -> EntityAlreadyExistsException("$entityName already exists. Cannot create a duplicate.")
            PSQLState.NOT_NULL_VIOLATION.state -> IllegalArgumentException("Invalid input. Required fields are missing.")
            PSQLState.CHECK_VIOLATION.state -> IllegalArgumentException("Invalid input. Check constraints failed.")
            else -> e
        }
    }

    @Throws(EntityAlreadyExistsException::class)
    /**
     * Pass the code to create a new entity as a lambda. Error handling is done automatically.
     * @param block The code to create a new entity.
     * @return The created entity
     * @throws EntityAlreadyExistsException If the entity already exists in the database.
     * @throws IllegalArgumentException If the input is invalid.
     * @throws PSQLException If a general database error occurs.
     * @throws Exception If an unknown error occurs.
     */
    suspend fun executeCreateBlock (block: suspend () -> T): T {
        return try {
            block()
        } catch (e: PSQLException) {
            throw handlePSQLException(e)
        }
        catch (e: Exception) {
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
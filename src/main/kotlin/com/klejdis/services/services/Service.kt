package com.klejdis.services.services

import com.klejdis.services.util.Image
import com.klejdis.services.util.MultiPartProcessResult
import com.klejdis.services.util.MultiPartProcessor
import io.ktor.http.content.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import org.koin.java.KoinJavaComponent.inject
import org.ktorm.entity.Entity
import org.postgresql.util.PSQLException
import org.postgresql.util.PSQLState
import java.util.*

/**
 * A general purpose service class for handling basic CRUD operations and exceptions. It is scoped to a specific business email, to simplify
 * the handling of entities that are related to a specific business. It also provides a custom exception handler for Postgres exceptions.
 * Can be extended to provide more specific functionality.
 * @param T The entity type to handle.
 * @property entityName The name of the entity to handle.
 * @property loggedInEmail The email of the currently logged in business user.
 * @property psqlExceptionHandler A custom exception handler for Postgres exceptions. Only set the fields that
 * need to be customized, the rest will be automatically set to default values
 * @constructor Creates a new service with the given entity name and an optional custom exception handler.
 */
abstract class Service<T : Entity<T>>(
    private val entityName: String,
    private var psqlExceptionHandler: PSQLExceptionHandler = PSQLExceptionHandler(),
    protected val loggedInEmail: String
) {
    val json by inject<Json>(Json::class.java)
    protected val imageStorePath = entityName.lowercase()
    protected val formExpectedName = entityName.lowercase()


    /**
     * Pass the code to create a new entity as a lambda. Error handling is done automatically through
     * the [handlePSQLException] method.
     * @param block The code to create a new entity.
     * @return The created entity.
     * @throws EntityAlreadyExistsException If the entity already exists in the database.
     * @throws IllegalArgumentException If the input is invalid.
     * @throws PSQLException If a general database error occurs.
     * @throws Exception If an unknown error occurs.
     */
    suspend fun executeCreateBlockWithErrorHandling(block: suspend () -> T): T {
        return try {
            block()
        } catch (e: PSQLException) {
            throw psqlExceptionHandler.handle(e)
        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("An unknown error occurred.")
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("An unknown error occurred.")
        }

    }

    @Throws(EntityNotFoundException::class)
    /**
     * Pass the code to update an entity as a lambda. Error handling is done automatically.
     * @param block The code to update an entity.
     * @return The updated entity
     * @throws EntityNotFoundException If a related entity does not exist.
     * @throws EntityAlreadyExistsException If the entity to be created already exists in the database.
     * @throws IllegalArgumentException If the input is invalid.
     * @throws PSQLException If a general database error occurs.
     * @throws Exception If an unknown error occurs.
     */
    suspend fun executeUpdateBlock(block: suspend () -> T): T {
        return try {
            block()
        } catch (e: PSQLException) {
            throw psqlExceptionHandler.handle(e)
        } catch (e: IllegalArgumentException) {
            throw e
        }
    }

    protected suspend inline fun<reified T: Any> deserializeFormAndSaveImage(
        multiPartData: MultiPartData,
        serializer: KSerializer<T>,
        formItemExpectedName: String? = null,
    ): MultiPartProcessResult<T, Image> {
        val deserializedFormAndImageData = MultiPartProcessor.getDeserializedFormAndImageData<T>(
            multiPartData,
            serializer,
            json,
            formItemExpectedName,
        )
        return deserializedFormAndImageData
    }
}


class PSQLExceptionHandler {

    /**
     * Handles a Postgres exception by checking the SQL state and returning the appropriate exception.
     * @param e The Postgres exception to handle.
     * @return The appropriate exception to throw.
     */
    fun handle(e: PSQLException): Exception {
        e.printStackTraceIfInDevMode()

        val errorMessage = structureErrorMessageData(e)
        return when (e.sqlState) {
            PSQLState.UNIQUE_VIOLATION.state -> EntityAlreadyExistsException(errorMessage)
            PSQLState.NOT_NULL_VIOLATION.state -> IllegalArgumentException(errorMessage)
            PSQLState.CHECK_VIOLATION.state -> IllegalArgumentException(errorMessage)
            PSQLState.FOREIGN_KEY_VIOLATION.state -> EntityNotFoundException(errorMessage)
            else -> Exception("An unknown database error occurred.")
        }
    }

    private fun extractEntityName(e: PSQLException): String {
        return e.serverErrorMessage?.table?.let { it ->
            val violation = e.serverErrorMessage?.constraint?.replace(it, "") ?: ""
            val entity = violation
                .substringAfter("_")
                .substringBefore("_")
                .replaceFirstChar { it.uppercase() }
            entity
        } ?: ""
    }

    private fun convertViolationToVerb(e: PSQLException): String {
        return when (e.sqlState) {
            PSQLState.UNIQUE_VIOLATION.state -> "already exists"
            PSQLState.NOT_NULL_VIOLATION.state -> "missing"
            PSQLState.CHECK_VIOLATION.state -> "failed check constraint"
            PSQLState.FOREIGN_KEY_VIOLATION.state -> "does not exist"
            else -> "Unknown violation"
        }
    }

    private fun extractField(e: PSQLException): String {
        return e.serverErrorMessage?.detail
            ?.substringAfter("(")
            ?.substringBefore(")")
            ?.replace("${extractEntityName(e).lowercase()}_", "")
            ?: ""
    }

    private fun extractFieldValue(e: PSQLException): String {
        return e.serverErrorMessage?.detail
            ?.substringAfter("=(")
            ?.substringBefore(")")
            ?: ""
    }

    private fun structureErrorMessageData(e: PSQLException): String {
        return "${extractEntityName(e)} with ${extractField(e)}=${extractFieldValue(e)} ${convertViolationToVerb(e)}."
    }


}

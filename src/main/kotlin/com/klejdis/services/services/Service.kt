package com.klejdis.services.services

import kotlin.jvm.Throws

/**
 * Interface for a service that provides the basic definition of CRUD operations for a given type.
 * @param T the type of the entity that the service is providing operations for.
 */
interface Service<T> {
    /**
     * @return a collection of all entities of type T.
     */
    suspend fun get(id: Int): T?
    /**
     * Creates a new entity of type T if it does not already exists and throws an exception if it does.
     * @return the newly created entity.
     */
    @Throws(EntityAlreadyExistsException::class)
    suspend fun create(entity: T) : T
    /**
     * Updates an entity of type T with the given id if it exists. Throws an exception if it does not.
     * @param id the id of the entity to update.
     * @return the updated entity.
     */
    @Throws(EntityNotFoundException::class)
    suspend fun update(id: Int) : T

    /**
     * Deletes an entity of type T with the given id if it exists.
     * @param id the id of the entity to delete.
     * @return true if the entity was deleted, false if no such entity exists.
     */
    suspend fun delete(id: Int) : Boolean
}
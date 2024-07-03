package com.klejdis.services.repositories

import com.klejdis.services.filters.Filter
import org.ktorm.entity.Entity

/**
 * A generic interface for CRUD operations on a database using Ktorm entities.
 * @param PK the type of the primary key of the entity
 * @param T the type of the entity
 * @see Entity
 */
interface CrudRepository<PK, T: Entity<T>> {
    /**
     * Gets all entities that match the filters
     * @param filters a list of filters that can be applied to the persistence layer
     * @return a list of entities that match the filters
     */
    suspend fun getAll(filters: Iterable<Filter> = emptySet()): List<T>

    /**
     * Gets an entity by its id(primary key)
     * @param id the id of the entity
     * @return the entity if it exists, null otherwise
     */
    suspend fun get(id: PK): T?

    /**
     * Creates a new entity. If everything goes well, the entity is returned with an updated primary key.
     * Otherwise, exceptions are thrown.
     * @param entity the entity to be created
     * @return the entity with the updated primary key
     */
    suspend fun create(entity: T): T

    /**
     * Updates an entity. If everything goes well, the entity is returned with the updated fields.
     * Otherwise, exceptions are thrown.
     * @param entity the entity to be updated
     * @return the entity with the updated fields
     */
    suspend fun update(entity: T): T

    /**
     * Deletes an entity by its id(primary key)
     * @param id the id of the entity
     * @return true if the entity was deleted, false otherwise
     */
    suspend fun delete(id: Int): Boolean
}
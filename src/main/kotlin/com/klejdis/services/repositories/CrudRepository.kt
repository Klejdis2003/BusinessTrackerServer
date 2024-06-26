package com.klejdis.services.repositories

interface CrudRepository<T> {
    suspend fun get(id: Int): T?
    suspend fun create(entity: T) : T
    suspend fun update(entity: T) : T
    suspend fun delete(id: Int) : Boolean
}
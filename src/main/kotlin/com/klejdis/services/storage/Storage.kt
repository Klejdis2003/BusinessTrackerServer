package com.klejdis.services.storage

interface Storage<T> {
    /**
     * Saves the item to the storage.
     * @param item The item to save.
     * @return The path where the item was saved.
     */
    suspend fun save(item: T): String
    suspend fun update(item: T)
    suspend fun delete(filename: String): Boolean
    suspend fun clearAll(): Boolean
}
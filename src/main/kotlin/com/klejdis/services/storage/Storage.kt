package com.klejdis.services.storage

interface Storage<T> {
    /**
     * Saves the item to the storage.
     * @param item The item to save.
     * @param generateRandomFilename Whether to generate a random filename for the item
     * @return The path where the item was saved.
     */
    fun save(item: T, generateRandomFilename: Boolean = true): String
    fun update(item: T)
    fun delete(filename: String): Boolean
    fun clearAll(): Boolean
}
package com.klejdis.services.storage

abstract class LocalStorage<T>(protected open val path: Path) : Storage<T> {
    abstract suspend fun save(item: T, generateRandomFileName: Boolean = true): String
}

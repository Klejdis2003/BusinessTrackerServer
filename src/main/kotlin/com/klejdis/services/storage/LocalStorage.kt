package com.klejdis.services.storage

abstract class LocalStorage<T>(protected open val path: Path) : Storage<T>

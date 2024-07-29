package com.klejdis.services.storage

import com.klejdis.services.util.FileOperations
import com.klejdis.services.util.Image
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object ItemImageStorage : ImageStorage(Path.Uploads.Images.Item)

open class ImageStorage (path: Path): LocalStorage<Image>(path) {

    fun generateImageName(): String =
        "${LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))}-${UUID.randomUUID()}"

    init {
        require(path.isStrictSubPathOf(Path.Uploads.Images)) { "The path must be a parent of Item" }
    }
    override suspend fun save(item: Image, generateRandomFileName: Boolean): String {
        FileOperations.createDirectoryIfNotExists(path)
        if(generateRandomFileName) item.name = generateImageName()

        val file = File(path.toFile(), item.getFullFileName())
        file.writeBytes(item.bytes)
        return file.path
    }
    override suspend fun save(item: Image): String {
        return save(item, true)
    }

    override suspend fun update(item: Image){
        save(item, false)
    }

    override suspend fun delete(filename: String): Boolean {
        val file = File(path.toFile(), filename)
        return file.delete()
    }

    override suspend fun clearAll(): Boolean {
        val directory = path.toFile()
        return directory.deleteRecursively()
    }

    fun getFullPathOf(filename: String?): String? {
        return filename?.let { "${path.value}/$it" }
    }

    fun clearAll(exceptions: Collection<String>) {
        val directory = path.toFile()
        directory.walk().forEach {
            if(it.isFile && exceptions.none { ex -> it.name.contains(ex) })
                it.delete()
        }
    }
    fun getPathAsFile() = path.toFile()
    fun getPathAsString() = path.value
}

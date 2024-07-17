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
    override fun save(item: Image, generateRandomFilename: Boolean): String {
        FileOperations.createDirectoryIfNotExists(path)
        if(generateRandomFilename) item.name = generateImageName()

        val file = File(path.toFile(), item.getFullFileName())
        file.writeBytes(item.bytes)
        return file.path
    }

    override fun update(item: Image){
        save(item, false)
    }


    override fun delete(filename: String): Boolean {
        val file = File(path.toFile(), filename)
        return file.delete()
    }

    override fun clearAll(): Boolean {
        val directory = path.toFile()
        return directory.deleteRecursively()
    }

    fun clearAll(exceptions: Set<String>) {
        val directory = path.toFile()
        directory.walk().forEach {
            if(it.isFile && it.name !in exceptions) it.delete()
        }
    }

    fun getPathAsFile() = path.toFile()
    fun getPathAsString() = path.value
}

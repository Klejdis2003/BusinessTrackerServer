package com.klejdis.services.util
import com.klejdis.services.storage.Path
import java.io.File

object FileOperations {
    fun readResourceFile(filePath: String): String {
        val loader = Thread.currentThread().contextClassLoader
        val inputStream = loader.getResourceAsStream(filePath)
        if (inputStream != null) {
            return inputStream.bufferedReader().use { it.readText() }
        }
        throw IllegalArgumentException("Resource file not found: $filePath")
    }

    fun getResourceAsRelativePath(filePath: String): String {
        val loader = Thread.currentThread().contextClassLoader
        val resourcePath = loader.getResource(filePath)?.path
            ?: throw IllegalArgumentException("Resource file not found: $filePath")
        return resourcePath
    }

    /**
     * Parses a SQL file and returns the content as a string.
     * @param fileName the name of the file to parse. Just the name,
     * with neither the path nor the extension.
     *
     */
    fun parseSqlFile(fileName: String): String {
        return readResourceFile("sql/$fileName.sql")
    }

    /**
     * Creates a directory if it does not exist.
     */

    fun createDirectoryIfNotExists(path: Path) {
        val dir = File(path.value)
        if (!dir.exists()) dir.mkdirs()
        println("Created directory ${dir.absolutePath}")
    }

}



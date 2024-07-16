package com.klejdis.services.util
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

object FileOperations {
    const val UPLOAD_DIR = "uploads"
    const val IMAGE_DIR = "${UPLOAD_DIR}/images"

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
        val resourcePath = loader.getResource("")?.path + filePath
        return resourcePath
    }

    /**
     * Removes all image files from the resources directory, only to be run in development, so no need
     * for relative paths.
     */
    fun removeAllImageFiles(): Boolean {
        val imageDir = Paths.get(IMAGE_DIR)
        try {
            val files = Files.walk(imageDir)
                .map { it.toFile() }
                .toList()
            files.forEach {
                if(it.isFile && !it.name.contains("default")){
                    it.delete()
                    println("Deleted file ${it.name}")
                }
            }
            return true
        }
        catch (e: Exception) {
            println("Error removing image files: ${e.message}")
            return false
        }
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

    private fun createDirectoryIfNotExists(path: String) {
        val dir = File(path)
        if (!dir.exists()) dir.mkdirs()
        println("Created directory ${dir.absolutePath}")
    }

    /**
     * Saves an image to the *resources* directory.
     * @param image The image to save.
     * @return The path where the image was saved.
     */
    fun saveImage(image: Image): String {
        val path = image.fullPath
        createDirectoryIfNotExists(File(path).parent)
        val file = File(path)
        file.writeBytes(image.bytes)
        return file.path
    }

}

data class Image(
    val name: String,
    val extension: String,
    val parentPath: String,
    val bytes: ByteArray
) {
    val fullFileName = "$name.$extension"
    val fullPath = "$parentPath/$fullFileName"
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Image

        if (name != other.name) return false
        if (!bytes.contentEquals(other.bytes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + bytes.contentHashCode()
        return result
    }
}


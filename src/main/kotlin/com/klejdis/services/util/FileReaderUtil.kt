package com.klejdis.services.util

fun loadResourceFile(filePath: String): String {
    val loader = Thread.currentThread().contextClassLoader
    val inputStream = loader.getResourceAsStream(filePath)
    if (inputStream != null) {
        return inputStream.bufferedReader().use { it.readText() }
    }
    throw IllegalArgumentException("Resource file not found: $filePath")
}

fun getResourceAsRelativePath(filePath: String): String {
    val loader = Thread.currentThread().contextClassLoader
    val resource = loader.getResource(filePath)
    return resource?.path ?: throw IllegalArgumentException("Resource file not found: $filePath")
}

/**
 * Parses a SQL file and returns the content as a string.
 * @param fileName the name of the file to parse. Just the name,
 * with neither the path nor the extension.
 *
 */
fun parseSqlFile(fileName: String): String {
    return loadResourceFile("sql/$fileName.sql")
}
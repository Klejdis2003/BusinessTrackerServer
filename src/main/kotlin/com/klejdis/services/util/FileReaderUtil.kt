package com.klejdis.services.util

import java.io.File

/**
 * Parses a SQL file and returns the content as a string.
 * @param fileName the name of the file to parse. Just the name,
 * with neither the path nor the extension.
 *
 */
fun parseSqlFile(fileName: String): String {
    val file = File("src/main/kotlin/com/klejdis/services/config/sql/$fileName.sql")
    return file.readText()
}
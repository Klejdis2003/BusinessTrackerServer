package com.klejdis.services.storage

import java.io.File
import java.util.*
import kotlin.reflect.KClass

/**
 * A sealed class that represents a path in the file system. It is a type safe way to represent
 * directories in the file system, when we need to work with them in the code.
 * The paths are defined as objects under the sealed class.
 * * Example usage:
 * ```
 * sealed class Path {
 *     val value: String
 *
 *     data object Uploads : Path() {
 *         data object Images : Path()
 *     }
 *     data object Images : Path()
 * ```
 * We can also have subdirectories, like in the example above. Images is accessed as **Path.Uploads.Images**.
 * @property value the path as a string. To see how it is generated, read the [getPath] method.
 */
sealed class Path {
    data object Uploads : Path() {
        data object Images : Path(){
            data object Item : Path()
        }
    }
    val value: String get() = getPath()
    /**
     * Uses reflection to get the path of the current class from the object hierarchy.
     * For example, if you say Path.Uploads.Images, it will return "uploads/images".
     * @return the path of the current class.
     */
    private fun getPath(): String {
        val parts: LinkedList<String> = LinkedList()
        var currentClass: KClass<*>? = this::class
        while(currentClass != Path::class && currentClass != null) {
            parts.addFirst(currentClass.simpleName!!.lowercase())
            currentClass = currentClass.objectInstance?.javaClass?.enclosingClass?.kotlin
        }
        return parts.joinToString(separator = "/")
    }


    /**
     * Converts the path to a file.
     * @return the path as a file.
     */
    fun toFile() = File(value)

    fun isParentPathOf(path: Path): Boolean {
        return path.value.startsWith(value)
    }

    fun isSubPathOf(path: Path): Boolean {
        return value.startsWith(path.value)
    }

    fun isStrictSubPathOf(path: Path): Boolean {
        return isSubPathOf(path) && value != path.value
    }

}
package com.klejdis.services.util

/**
 * Represents an image file.
 * @param name The name of the image.
 * @param format The file format of the image, e.g. "png".
 * @param bytes The byte array that contains the image data.
 * @property fullFileName [name].[format], e.g. "image.png".
 */
data class Image(
    var name: String,
    val format: String,
    val bytes: ByteArray
) {
    fun getFullFileName() = "$name.$format"
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

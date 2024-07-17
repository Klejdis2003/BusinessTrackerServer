package com.klejdis.services.dto

import com.klejdis.services.APPLICATION_DOMAIN
import com.klejdis.services.plugins.DEFAULT_IMAGES_ENDPOINT
import com.klejdis.services.storage.Path


/**
 * A class that maps a path to a link. It is used to generate links to images stored in the file system.
 * @property endpoint the endpoint of the link. It will be appended to the [APPLICATION_DOMAIN] to form the full link.
 */
class ImageLinkMapper(private val endpoint: String) {
    fun mapPathToLink(path: Path): String =
        "$APPLICATION_DOMAIN/$endpoint/$path"

    fun mapPathToLink(path: String?): String {
        val filname = path?.substringAfterLast("/")
        return path?.let { "$APPLICATION_DOMAIN/$endpoint/$filname" }
            ?: "$APPLICATION_DOMAIN/$DEFAULT_IMAGES_ENDPOINT/default.jpg"
    }

}
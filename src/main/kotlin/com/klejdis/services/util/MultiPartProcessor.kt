package com.klejdis.services.util

import io.ktor.http.content.*
import io.ktor.utils.io.core.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

/**
 * Represents the result of processing multipart data.
 * @param formItemData The form item data that was processed.
 * @param fileItemData The path where the multipart data was saved.
 */
data class MultiPartProcessResult<T, K>(
    val formItemData: T?,
    val fileItemData: K?
)


object MultiPartProcessor {

    /**
     * Handles the form item data from the multipart data.
     * @param formItem The form item data to handle.
     * @return The form item data.
     */
    @PublishedApi
    internal inline fun<reified T> handleFormItemData(
        formItem: PartData.FormItem,
        formItemExpectedName: String,
        json: Json,
        serializer: KSerializer<T>,
    ): T {
        val formItemName = formItemExpectedName ?: T::class.simpleName
        if (formItemName != formItem.name) throw IllegalArgumentException("Expected data $formItemName not provided.")
        return json.decodeFromString(serializer, formItem.value)
    }

    @PublishedApi
    internal suspend fun handleFileData(fileItem: PartData.FileItem): Image {
        val originalFileFormat = fileItem.originalFileName?.split(".")?.last()
        val name = fileItem.originalFileName?.substringBeforeLast(".")
        return Image(
            name = name!!,
            format = originalFileFormat!!,
            bytes = processMultiPartImage(fileItem),
        )
    }


     suspend fun processMultiPartImage(fileItem: PartData.FileItem): ByteArray {
        return fileItem.provider().readRemaining().readBytes()
    }

    /**
     * Saves the image part of the multipart data to the resources directory. Also exposes the form item data
     * to a lambda for further processing.
     * @param multipart The multipart data to save.
   name of the form field that you expect to contain the other form data.
     * @param handleFormItemData A lambda that handles form item data, if the form is expected to contain more
     * than just the file.
     * @param handleFileData A lambda that handles the file data. By default, it saves the file to the resources.
     * @return The path where the multipart data was saved.
     */
    suspend fun<T, K> process(
        multipart: MultiPartData,
        handleFormItemData: suspend (PartData.FormItem) -> T,
        handleFileData : suspend (PartData.FileItem) -> K
    ): MultiPartProcessResult<T, K> {
        var formResult: T? = null
        var fileResult: K? = null
        var formItemAdded = false
        var imageAdded = false
        multipart.forEachPart { part ->
            when (part) {
                is PartData.FormItem -> {
                    formResult = handleFormItemData(part)
                    formItemAdded = true
                }

                is PartData.FileItem -> {
                    fileResult = handleFileData(part)
                    imageAdded = true
                }
                else -> {}
            }
            part.dispose()
            if (formItemAdded && imageAdded) return@forEachPart
        }
        return MultiPartProcessResult(formResult, fileResult)
    }


    /**
     * Processes a form and saves the image file from the multipart data.
     * @param multipart The multipart data to process.
     * For example, if the path is "items", the multipart data will be saved to src/main/resources/images/items.
     * @param serializer The serializer to use to deserialize the form data.
     * @return The form data and the path where the multipart data was saved. The Image portion of the
     * result contains the original file name, the file format, and the bytes of the file.
     */
    suspend inline fun <reified T : Any> getDeserializedFormAndImageData(
        multipart: MultiPartData,
        serializer: KSerializer<T>,
        json: Json = Json.Default,
        formItemExpectedName: String? = null,
    ): MultiPartProcessResult<T, Image> =
        process(
            multipart = multipart,
            handleFormItemData = { formItem ->
                handleFormItemData(
                    formItem,
                    formItemExpectedName ?: T::class.simpleName!!,
                    json,
                    serializer)},
            handleFileData = { fileItem -> handleFileData(fileItem) }
        )

    suspend fun getImage(
        multipart: MultiPartData
    ): Image? {
        val result = process(
            multipart = multipart,
            handleFormItemData = { throw IllegalArgumentException("No form data expected.") },
            handleFileData = { fileItem -> handleFileData(fileItem) }
        )
        return result.fileItemData
    }
}





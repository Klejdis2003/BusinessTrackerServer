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
     * @param path The path to save the multipart data to. Defaults to the resources directory.
     * For example, if the path is "items", the multipart data will be saved to src/main/resources/images/items.
     * @param serializer The serializer to use to deserialize the form data.
     * @param imageName The name of the image file. If not provided, the original file name from the multipart
     * data will be used.
     * @return The form data and the path where the multipart data was saved.
     */
    suspend inline fun <reified T : Any> getDeserializedFormAndImageData(
        multipart: MultiPartData,
        path: String,
        serializer: KSerializer<T>,
        json: Json = Json.Default,
        formItemExpectedName: String? = null,
        imageName: String? = null
    ): MultiPartProcessResult<T, Image> =
        process(
            multipart = multipart,
            handleFormItemData = { formItem ->
                val formItemName = formItemExpectedName ?: T::class.simpleName
                if (formItemName != formItem.name) throw IllegalArgumentException("Expected data $formItemName not provided.")
                json.decodeFromString(serializer, formItem.value)
            },
            handleFileData = { fileItem ->
                val originalFileFormat = fileItem.originalFileName?.split(".")?.last()
                val name = imageName ?: fileItem.originalFileName?.substringBeforeLast(".")
                val imagePath = "${FileOperations.IMAGE_DIR}/$path"
                Image(
                    name = name!!,
                    extension = originalFileFormat!!,
                    bytes = processMultiPartImage(fileItem),
                    parentPath = imagePath
                )
            }
        )
    }





package com.klejdis.services.services

import com.klejdis.services.dto.ItemCreationDto
import com.klejdis.services.dto.ItemDto
import com.klejdis.services.filters.Filter
import com.klejdis.services.model.Item
import com.klejdis.services.repositories.BusinessRepository
import com.klejdis.services.repositories.ItemRepository
import com.klejdis.services.util.FileOperations
import io.ktor.http.content.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class ItemService(
    private val itemRepository: ItemRepository,
    private val businessRepository: BusinessRepository,
    loggedInEmail: String
) : Service<Item>(
    entityName = "Item",
    loggedInEmail = loggedInEmail
) {
    suspend fun getAll(filters: Iterable<Filter>) =
        itemRepository
            .getByBusinessOwnerEmail(loggedInEmail, filters)
            .map { ItemDto.fromEntity(it) }

    suspend fun get(id: Int) =
        itemRepository
            .get(id)
            .takeIf {
                println(it?.business ?: "No business found")
                it?.business?.ownerEmail == loggedInEmail
            }
            ?.let { ItemDto.fromEntity(it) }

    suspend fun get(filename: String) =
        itemRepository
            .getByImageUrl(filename)
            .takeIf {
                it?.business?.ownerEmail == loggedInEmail
            }
            ?.let { ItemDto.fromEntity(it) }
    suspend fun create(item: ItemCreationDto): ItemDto{
        val business = businessRepository.getByEmail(loggedInEmail) ?: throw EntityNotFoundException("Business not found")
        val createdItem = executeCreateBlockWithErrorHandling {
            itemRepository.create(ItemCreationDto.toEntity(item).apply { this.business = business })
        }
        return ItemDto.fromEntity(createdItem)
    }

    suspend fun create(multiPartData: MultiPartData): ItemDto {
        val multiPartProcessResult = deserializeFormAndSaveImage<ItemCreationDto>(
            multiPartData = multiPartData,
            path = "items",
            serializer = ItemCreationDto.serializer(),
            formItemExpectedName = "item",
            imageName = "item${System.currentTimeMillis()}"
        )
        if(multiPartProcessResult.formItemData == null || multiPartProcessResult.fileItemData == null) throw IllegalArgumentException("Item data not found")

        val business = businessRepository.getByEmail(loggedInEmail) ?: throw EntityNotFoundException("Business not found")
        val item = ItemCreationDto.toEntity(multiPartProcessResult.formItemData).apply { this.business = business }
        val imageName = "${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))}-${UUID.randomUUID()}"
        val image = multiPartProcessResult.fileItemData.copy(name = imageName)
        item.imageFilename = image.fullFileName

        val createdItem = executeCreateBlockWithErrorHandling { itemRepository.create(item) }
        FileOperations.saveImage(image)
        return ItemDto.fromEntity(createdItem)
    }

    suspend fun update(item: Item) =
        executeUpdateBlock { itemRepository.update(item) }

    suspend fun delete(id: Int) =
        itemRepository.delete(id)

}
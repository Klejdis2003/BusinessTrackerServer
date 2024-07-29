package com.klejdis.services.services

import com.klejdis.services.dto.ItemCreationDto
import com.klejdis.services.dto.ItemDto
import com.klejdis.services.dto.ItemMapper
import com.klejdis.services.filters.Filter
import com.klejdis.services.model.Item
import com.klejdis.services.repositories.BusinessRepository
import com.klejdis.services.repositories.ItemRepository
import com.klejdis.services.storage.ItemImageStorage
import com.klejdis.services.util.MultiPartProcessor
import io.ktor.http.content.*


class ItemService(
    private val itemRepository: ItemRepository,
    private val businessRepository: BusinessRepository,
    private val itemMapper: ItemMapper,
    loggedInEmail: String
) : Service<Item>(
    entityName = "Item",
    loggedInEmail = loggedInEmail
) {
    suspend fun getAll(filters: Iterable<Filter>) =
        itemRepository
            .getAll(filters)
            .map { itemMapper.toItemDto(it) }

    suspend fun get(id: Int) =
        itemRepository
            .get(id)
            .takeIf {
                println(it?.business ?: "No business found")
                it?.business?.ownerEmail == loggedInEmail
            }
            ?.let { itemMapper.toItemDto(it) }

    suspend fun get(filename: String) =
        itemRepository
            .getByImageUrl(filename)
            .takeIf {
                it?.business?.ownerEmail == loggedInEmail
            }
            ?.let { itemMapper.toItemDto(it) }
    suspend fun create(item: ItemCreationDto): ItemDto{
        val business = businessRepository.getByEmail(loggedInEmail) ?: throw EntityNotFoundException("Business not found")
        val createdItem = executeCreateBlockWithErrorHandling {
            itemRepository.create(ItemCreationDto.toEntity(item).apply { this.business = business })
        }
        return itemMapper.toItemDto(createdItem)
    }

    suspend fun create(multiPartData: MultiPartData): ItemDto {
        val multiPartProcessResult = deserializeFormAndSaveImage<ItemCreationDto>(
            multiPartData = multiPartData,
            serializer = ItemCreationDto.serializer(),
            formItemExpectedName = formExpectedName
        )
        if(multiPartProcessResult.formItemData == null || multiPartProcessResult.fileItemData == null) throw IllegalArgumentException("Item data not found")

        val business = businessRepository.getByEmail(loggedInEmail) ?: throw EntityNotFoundException("Business not found")
        val item = ItemCreationDto.toEntity(multiPartProcessResult.formItemData).apply { this.business = business }

        val image = multiPartProcessResult.fileItemData.apply { name = ItemImageStorage.generateImageName() }
        item.imageFilename = image.getFullFileName()
        val createdItem = executeCreateBlockWithErrorHandling { itemRepository.create(item) }
        ItemImageStorage.save(image, generateRandomFileName = false)

        return itemMapper.toItemDto(createdItem)
    }

    suspend fun updateImage(itemId: Int, multiPartData: MultiPartData): ItemDto {
        val item = itemRepository.get(itemId) ?: throw EntityNotFoundException("Item not found")
        if(item.business.ownerEmail != loggedInEmail) throw UnauthorizedException()
        val image = MultiPartProcessor.getImage(multiPartData) ?: throw IllegalArgumentException("Image not found")
        val updatedItem = executeUpdateBlock { itemRepository.update(item) }
        ItemImageStorage.update(image)
        return itemMapper.toItemDto(updatedItem)
    }

    suspend fun delete(id: Int){
        val item = itemRepository.get(id) ?: throw EntityNotFoundException("Item not found")
        if(itemRepository.delete(id)) item.imageFilename?.let { ItemImageStorage.delete(it) }
    }

}
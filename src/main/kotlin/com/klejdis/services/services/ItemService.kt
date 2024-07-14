package com.klejdis.services.services

import com.klejdis.services.dto.ItemCreationDto
import com.klejdis.services.dto.ItemDto
import com.klejdis.services.filters.Filter
import com.klejdis.services.model.Item
import com.klejdis.services.repositories.BusinessRepository
import com.klejdis.services.repositories.ItemRepository

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
            .takeIf { it?.business?.ownerEmail == loggedInEmail }
            ?.let { ItemDto.fromEntity(it) }

    suspend fun create(item: ItemCreationDto): ItemDto{
        val business = businessRepository.getByEmail(loggedInEmail) ?: throw EntityNotFoundException("Business not found")
        val createdItem = executeCreateBlockWithErrorHandling {
            itemRepository
                .create(
                    ItemCreationDto.toEntity(item).apply { this.business = business })
        }
        return ItemDto.fromEntity(createdItem)
    }

    suspend fun update(item: Item) =
        executeUpdateBlock { itemRepository.update(item) }

    suspend fun delete(id: Int) =
        itemRepository.delete(id)
}
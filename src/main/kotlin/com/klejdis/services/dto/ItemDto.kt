package com.klejdis.services.dto

import com.klejdis.services.model.Item
import com.klejdis.services.model.ItemType
import com.klejdis.services.sort.SortableEntity
import com.klejdis.services.sort.SortableField
import com.klejdis.services.storage.ItemImageStorage
import kotlinx.serialization.Serializable
import java.util.*

class ItemMapper(private val imageLinkMapper: ImageLinkMapper) {
    fun toItemDto(item: Item): ItemDto {
        val fullPath = ItemImageStorage.getFullPathOf(item.imageFilename)
        val imageUrl = imageLinkMapper.mapPathToLink(fullPath)
        return ItemDto.fromEntity(item, imageUrl)
    }
}

@Serializable
data class ItemDto(
    @SortableField val id: Int,
    @SortableField val name: String,
    @SortableField val purchasePrice: Int,
    @SortableField val price: Int,
    val currency: CurrencyDto,
    val imageUrl: String,
    val type: ItemTypeDto
) : SortableEntity {
    companion object {
        fun fromEntity(item: Item, imageUrl: String): ItemDto {
            return ItemDto(
                id = item.id,
                name = item.name,
                purchasePrice = item.purchasePrice,
                price = item.price,
                currency = CurrencyDto.fromCurrency(Currency.getInstance(item.currency)),
                type = ItemTypeDto(
                    id = item.type.id,
                    name = item.type.name,
                    description = item.type.description
                ),
                imageUrl = imageUrl
            )
        }
    }
}

@Serializable
data class ItemCreationDto(
    val name: String,
    val purchasePrice: Int,
    val price: Int,
    val currencySymbol: String,
    val typeId: Int
) {
    companion object: Mappable<Item, ItemCreationDto> {
        override fun fromEntity(entity: Item): ItemCreationDto {
            return ItemCreationDto(
                name = entity.name,
                purchasePrice = entity.purchasePrice,
                price = entity.price,
                currencySymbol = entity.currency,
                typeId = entity.type.id
            )
        }

        override fun toEntity(dto: ItemCreationDto): Item {
            return Item {
                this.name = dto.name
                this.purchasePrice = dto.purchasePrice
                this.price = dto.price
                this.currency = dto.currencySymbol
                this.type = ItemType {
                    this.id = dto.typeId
                }
            }
        }

    }
}

@Serializable
data class ItemTypeDto(
    val id: Int,
    val name: String,
    val description: String
)

@Serializable
data class Image(
    val url: String,
    val fileExtension: String,
)
package com.klejdis.services.dto

import com.klejdis.services.APPLICATION_DOMAIN
import com.klejdis.services.model.Item
import com.klejdis.services.model.ItemType
import com.klejdis.services.routes.ITEM_IMAGES_ENDPOINT
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class ItemDto(
    val id: Int,
    val name: String,
    val purchasePrice: Int,
    val price: Int,
    val currency: CurrencyDto,
    val imageUrl: String,
    val type: ItemTypeDto
) {
    companion object {
        fun fromEntity(item: Item): ItemDto {
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
                imageUrl = item.imageFilename?.let {"$APPLICATION_DOMAIN/${ITEM_IMAGES_ENDPOINT}/${item.imageFilename}"}
                    ?: "$APPLICATION_DOMAIN/${ITEM_IMAGES_ENDPOINT}/default.jpg"
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

class ItemMapper {
    fun toItemDto(item: Item): ItemDto {
        return ItemDto(
            id = item.id,
            name = item.name,
            purchasePrice = item.purchasePrice,
            price = item.price,
            currency = CurrencyMapper.toDto(Currency.getInstance(item.currency)),
            type = ItemTypeDto(
                id = item.type.id,
                name = item.type.name,
                description = item.type.description
            ),
            imageUrl = item.imageFilename ?: "default.jpg"
        )
    }

    fun toEntity(dto: ItemDto): Item {
        return Item {
            this.id = dto.id
            this.name = dto.name
            this.purchasePrice = dto.purchasePrice
            this.price = dto.price
            this.currency = dto.currency.code
            this.type = ItemType {
                this.id = dto.type.id
                this.name = dto.type.name
                this.description = dto.type.description
            }
        }
    }


}
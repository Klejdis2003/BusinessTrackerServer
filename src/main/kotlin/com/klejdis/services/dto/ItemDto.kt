package com.klejdis.services.dto

import com.klejdis.services.model.Item
import com.klejdis.services.model.ItemType
import kotlinx.serialization.Serializable
import java.util.Currency

@Serializable
data class ItemDto(
    val id: Int,
    val name: String,
    val purchasePrice: Int,
    val price: Int,
    val currency: CurrencyDto,
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
                )
            )
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
            )
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
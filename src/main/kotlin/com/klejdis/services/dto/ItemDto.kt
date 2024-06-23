package com.klejdis.services.dto

import com.klejdis.services.model.Business
import com.klejdis.services.model.Item
import com.klejdis.services.model.ItemType
import kotlinx.serialization.Serializable

@Serializable
class ItemDto(
    val id: Int,
    val name: String,
    val purchasePrice: Int,
    val price: Int,
    val type: String
)

class ItemMapper{
    fun toItemDto(item: Item): ItemDto {
        return ItemDto(
            id = item.id,
            name = item.name,
            purchasePrice = item.purchasePrice,
            price = item.price,
            type = item.type.name
        )
    }

}
package com.klejdis.services.dto

import com.klejdis.services.model.Order
import kotlinx.serialization.Serializable

@Serializable
data class OrderDto(
    val id: Int,
    val date: String,
    val items: List<ItemDto>
)

class OrderMapper(
    private val itemMapper: ItemMapper
){
    fun toOrderDto(order: Order): OrderDto {
        return OrderDto(
            id = order.id,
            date = order.date.toString(),
            items = order.items.map { itemMapper.toItemDto(it) }
        )
    }

}
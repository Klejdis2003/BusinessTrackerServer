package com.klejdis.services.dto

import com.klejdis.services.model.Order
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class OrderDto(
    val id: Int = 0,
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

    fun toEntity(dto: OrderDto): Order {
        return Order {
            this.id = dto.id
            this.date = LocalDate.parse(dto.date)
            this.items = dto.items.map { itemMapper.toEntity(it) }
        }
    }

}
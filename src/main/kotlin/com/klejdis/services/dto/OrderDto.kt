package com.klejdis.services.dto

import com.klejdis.services.model.Order
import com.klejdis.services.model.OrderItem
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class OrderDto(
    val id: Int = 0,
    val date: String,
    val items: List<OrderItemDto>
)

@Serializable
data class OrderItemDto(
    val item: ItemDto,
    val quantity: Int
)

class OrderMapper(
    private val itemMapper: ItemMapper
){
    fun toOrderDto(order: Order): OrderDto {
        return OrderDto(
            id = order.id,
            date = order.date.toString(),
            items = order.items.map {
                OrderItemDto(
                    item = itemMapper.toItemDto(it.item),
                    quantity = it.quantity
                )
            }
        )
    }

    fun toEntity(dto: OrderDto): Order {
        return Order {
            this.id = dto.id
            this.date = LocalDate.parse(dto.date)
            this.items = dto.items.map {
                OrderItem(
                    item = itemMapper.toEntity(it.item),
                    quantity = it.quantity
                )
            }
        }
    }

}
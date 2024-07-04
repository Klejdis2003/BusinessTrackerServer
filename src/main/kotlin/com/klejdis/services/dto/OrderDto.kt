package com.klejdis.services.dto

import com.klejdis.services.model.*
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class OrderDto(
    val id: Int = 0,
    val date: String,
    val customer: CustomerDto,
    val items: List<OrderItemDto>,
    val total: Int
)

@Serializable
data class OrderCreationDto(
    val date: String,
    val customerPhone: String,
    val items: List<OrderCreationItemDto>
) {
    init {
        require(items.isNotEmpty()) { "Order must have at least one item." }
        require(customerPhone.length == 10) { "Customer phone number must be 10 digits long." }
    }

}

@Serializable
data class OrderItemDto(
    val item: ItemDto,
    val quantity: Int
)

@Serializable
data class OrderCreationItemDto(
    val itemId: Int,
    val quantity: Int
)

class OrderMapper(
    private val itemMapper: ItemMapper,
    private val customerMapper: CustomerMapper
) {
    fun toOrderDto(order: Order): OrderDto {
        return OrderDto(
            id = order.id,
            date = order.date.toString(),
            customer = customerMapper.toCustomerDto(order.customer),
            total = order.total,
            items = order.items.map {
                OrderItemDto(
                    item = itemMapper.toItemDto(it.item),
                    quantity = it.quantity
                )
            }
        )
    }


    fun toEntity(dto: OrderCreationDto, business: Business): Order {
        return Order {
            this.date = LocalDate.parse(dto.date)
            this.items = dto.items.map {
                OrderItem(
                    item = Item{ id = it.itemId },
                    quantity = it.quantity
                )
            }
            this.customer = Customer { phone = dto.customerPhone }
            this.business = business
        }
    }

    fun toEntity(dto: OrderDto, business: Business): Order {
        return Order {
            this.id = dto.id
            this.date = LocalDate.parse(dto.date)
            this.items = dto.items.map {
                OrderItem(
                    item = Item{ id = it.item.id },
                    quantity = it.quantity
                )
            }
            this.customer = customerMapper.toEntity(dto.customer)
            this.business = business
        }
    }

}
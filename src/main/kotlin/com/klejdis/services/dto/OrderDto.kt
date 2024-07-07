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
    val total: Int,
    val business: Business? = null
) {
    companion object : Mappable<Order, OrderDto>{
        override fun fromEntity(entity: Order): OrderDto {
            return OrderDto(
                id = entity.id,
                date = entity.date.toString(),
                customer = CustomerDto.fromEntity(entity.customer),
                items = entity.items.map {
                    OrderItemDto(
                        item = ItemDto.fromEntity(it.item),
                        quantity = it.quantity
                    )
                },
                total = entity.total
            )
        }

        override fun toEntity(dto: OrderDto): Order {
            if(dto.business == null) throw IllegalArgumentException("Business must be provided, but was null.")
            return Order {
                this.id = dto.id
                this.date = LocalDate.parse(dto.date)
                this.items = dto.items.map {
                    OrderItem(
                        item = Item{ id = it.item.id },
                        quantity = it.quantity
                    )
                }
                this.customer = CustomerDto.toEntity(dto.customer)
                this.business = dto.business
            }

        }
    }
}

@Serializable
data class OrderCreationDto(
    val date: String,
    val customerPhone: String,
    val items: List<OrderCreationItemDto>,
    val business: Business? = null
) {
    init {
        require(items.isNotEmpty()) { "Order must have at least one item." }
        require(customerPhone.length == 10) { "Customer phone number must be 10 digits long." }
    }

    companion object: Mappable<Order, OrderCreationDto>{

        /**
         * Converts an Order entity to an OrderCreationDto.
         * @param entity The Order entity to convert
         * @return The OrderCreationDto representation of the entity
         */
        override fun fromEntity(entity: Order): OrderCreationDto {
            return OrderCreationDto(
                date = entity.date.toString(),
                customerPhone = entity.customer.phone,
                items = entity.items.map {
                    OrderCreationItemDto(
                        itemId = it.item.id,
                        quantity = it.quantity
                    )
                }
            )
        }

        /**
         * Converts an OrderCreationDto to an Order entity. Make sure to set the business property of the dto before calling this method.
         * @param dto The OrderCreationDto to convert
         * @return The Order entity representation of the dto
         * @throws IllegalArgumentException if the business property of the dto is null
         */
        override fun toEntity(dto: OrderCreationDto): Order {
            if(dto.business == null) throw IllegalArgumentException("Business must be provided, but was null.")
            return Order {
                this.date = LocalDate.parse(dto.date)
                this.items = dto.items.map {
                    OrderItem(
                        item = Item{ id = it.itemId },
                        quantity = it.quantity
                    )
                }
                this.customer = Customer { phone = dto.customerPhone }
                this.business = dto.business
            }
        }

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
package com.klejdis.services.dto

import com.klejdis.services.model.Business
import com.klejdis.services.model.Customer
import com.klejdis.services.model.Order
import com.klejdis.services.model.OrderItem
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class OrderDto(
    val id: Int = 0,
    val date: String,
    val customer: CustomerDto,
    val items: List<OrderItemDto>
)

@Serializable
data class OrderCreationDto(
    val date: String,
    val customerPhone: String,
    val items: List<OrderItemDto>
)

@Serializable
data class OrderItemDto(
    val item: ItemDto,
    val quantity: Int
)

class OrderMapper(
    private val itemMapper: ItemMapper,
    private val customerMapper: CustomerMapper
){
    fun toOrderDto(order: Order): OrderDto {
        return OrderDto(
            id = order.id,
            date = order.date.toString(),
            customer = customerMapper.toCustomerDto(order.customer),
            items = order.items.map {
                OrderItemDto(
                    item = itemMapper.toItemDto(it.item),
                    quantity = it.quantity
                )
            }
        )
    }

    fun toOrderCreationDto(order: Order): OrderCreationDto {
        return OrderCreationDto(
            date = order.date.toString(),
            customerPhone = order.customer.phone,
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
                    item = itemMapper.toEntity(it.item),
                    quantity = it.quantity
                )
            }
            this.customer = Customer{phone = dto.customerPhone}
            this.business = business
        }
    }

    fun toEntity(dto: OrderDto, business: Business): Order {
        return Order {
            this.id = dto.id
            this.date = LocalDate.parse(dto.date)
            this.items = dto.items.map {
                OrderItem(
                    item = itemMapper.toEntity(it.item),
                    quantity = it.quantity
                )
            }
            this.customer = customerMapper.toEntity(dto.customer)
            this.business = business
        }
    }

}
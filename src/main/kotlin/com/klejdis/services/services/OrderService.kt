package com.klejdis.services.services

import com.klejdis.services.dto.OrderCreationDto
import com.klejdis.services.dto.OrderDto
import com.klejdis.services.dto.OrderMapper
import com.klejdis.services.filters.Filter
import com.klejdis.services.model.Order
import com.klejdis.services.model.OrderItem
import com.klejdis.services.repositories.BusinessRepository
import com.klejdis.services.repositories.ItemRepository
import com.klejdis.services.repositories.OrderRepository

class OrderService(
    private val orderRepository: OrderRepository,
    private val businessRepository: BusinessRepository,
    private val itemRepository: ItemRepository,
    private val orderMapper: OrderMapper,
    loggedInEmail: String
) : Service<Order>(
    entityName = "Order",
    loggedInEmail = loggedInEmail
) {
    /**
     * @param id Order id
     * @param email The email of the business owner whose order is being fetched
     * @return OrderDto if the order exists and belongs to the business owner, null otherwise
     */
    suspend fun get(id: Int): OrderDto? {
        val order = orderRepository.get(id)
        return order?.takeIf { it.business.ownerEmail == loggedInEmail }?.let { orderMapper.toOrderDto(it) }
    }
    suspend fun getByBusinessId(businessId: Int) =
        orderRepository.filterByBusinessId(businessId).map { orderMapper.toOrderDto(it) }

    suspend fun getAllBusinessOrders(filters: Iterable<Filter>) =
        orderRepository.filterByBusinessOwnerEmail(loggedInEmail, filters).map { orderMapper.toOrderDto(it) }



    suspend fun getMostExpensiveOrder() =
        orderRepository.getMostExpensiveByBusinessOwnerEmail(loggedInEmail)?.let { orderMapper.toOrderDto(it) }

    @Throws(EntityAlreadyExistsException::class)
    suspend fun create(dto: OrderCreationDto): OrderDto {

        //make necessary checks and get needed values
        val business = businessRepository.getByEmail(loggedInEmail)
            ?: throw EntityNotFoundException("Business with email=$loggedInEmail does not exist")
        val dto = dto.copy(business = business)
        var order = OrderCreationDto.toEntity(dto)

        //get items from their ids and create OrderItem objects
        val itemIds = dto.items.map { it.itemId }
        order.items = itemRepository.getBatch(itemIds).map { item ->
            OrderItem(
                item = item,
                quantity = dto.items.first { it.itemId == item.id }.quantity
            )
        }.run { if (size != itemIds.size) throw EntityNotFoundException("One or more items do not exist") else this}

        //create the order and return the OrderDto
        order = super.executeCreateBlockWithErrorHandling {
            orderRepository.create(order)
        }
        return orderMapper.toOrderDto(order)
    }

    suspend fun addOrderItems(orderId: Int, items: List<OrderItem>): OrderDto {
        val order = orderRepository.get(orderId)
            ?: throw EntityNotFoundException("Order with id=$orderId does not exist")
        if (order.business.ownerEmail != loggedInEmail)
            throw UnauthorizedException("Unauthorized action! Order with id=$orderId does not belong to the business with email=$loggedInEmail.")
        order.items += items
        orderRepository.update(order)
        return orderMapper.toOrderDto(order)
    }



    suspend fun delete(id: Int) = orderRepository.delete(id)
}
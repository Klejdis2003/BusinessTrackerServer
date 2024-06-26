package com.klejdis.services.services

import com.klejdis.services.dto.OrderDto
import com.klejdis.services.dto.OrderMapper
import com.klejdis.services.model.Order
import com.klejdis.services.repositories.OrderRepository
import kotlin.jvm.Throws

class OrderService(
    private val orderRepository: OrderRepository,
    private val orderMapper: OrderMapper
): Service<Order>("Order") {
    suspend fun get(id: Int) =
        orderRepository.get(id)?.let { orderMapper.toOrderDto(it) }
    suspend fun getByBusinessId(businessId: Int) =
        orderRepository.getByBusinessId(businessId).map { orderMapper.toOrderDto(it) }
    suspend fun getByBusinessOwnerEmail(email: String) =
        orderRepository.getByBusinessOwnerEmail(email).map { orderMapper.toOrderDto(it) }
    suspend fun getByIdAndBusinessOwnerEmail(id: Int, email: String) =
        orderRepository.getByIdAndBusinessOwnerEmail(id, email)?.let { orderMapper.toOrderDto(it) }

    @Throws(EntityAlreadyExistsException::class)
    suspend fun create(dto: OrderDto): OrderDto {
        val order = super.executeCreateBlock {
            orderRepository.create(orderMapper.toEntity(dto))
        }
        return orderMapper.toOrderDto(order)
    }
    suspend fun update(dto: OrderDto) = orderRepository.update(orderMapper.toEntity(dto)).let { orderMapper.toOrderDto(it) }
    suspend fun delete(id: Int) = orderRepository.delete(id)
}
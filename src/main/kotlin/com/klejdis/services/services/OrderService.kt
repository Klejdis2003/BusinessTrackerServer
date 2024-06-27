package com.klejdis.services.services

import com.klejdis.services.dto.OrderCreationDto
import com.klejdis.services.dto.OrderDto
import com.klejdis.services.dto.OrderMapper
import com.klejdis.services.model.Order
import com.klejdis.services.repositories.BusinessRepository
import com.klejdis.services.repositories.OrderRepository

class OrderService(
    private val orderRepository: OrderRepository,
    private val businessRepository: BusinessRepository,
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
    suspend fun create(dto: OrderCreationDto, businessEmail: String): OrderDto {
        val business = businessRepository.getByEmail(businessEmail) ?: throw EntityNotFoundException("Business with email=$businessEmail does not exist")
        val order = super.executeCreateBlockWithErrorHandling {
            orderRepository.create(orderMapper.toEntity(dto, business))
        }
        return orderMapper.toOrderDto(order)
    }
    suspend fun update(dto: OrderDto, businessEmail: String) {
        val business = businessRepository.getByEmail(businessEmail) ?: throw EntityNotFoundException("Business with email=$businessEmail does not exist")
        orderRepository.update(orderMapper.toEntity(dto, business)).let { orderMapper.toOrderDto(it) }
    }
    suspend fun delete(id: Int) = orderRepository.delete(id)
}
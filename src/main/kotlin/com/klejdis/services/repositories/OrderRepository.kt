package com.klejdis.services.repositories

import com.klejdis.services.model.Order

interface OrderRepository : CrudRepository<Order> {
    suspend fun getByBusinessId(businessId: Int): List<Order>
    suspend fun getByBusinessOwnerEmail(email: String, filters: Map<String, String> = emptyMap()): List<Order>
    suspend fun getByIdAndBusinessOwnerEmail(id: Int, email: String): Order?
}
package com.klejdis.services.repositories

import com.klejdis.services.filters.Filter
import com.klejdis.services.model.Order

interface OrderRepository : CrudRepository<Int, Order> {
    suspend fun filterByBusinessId(businessId: Int): List<Order>
    suspend fun filterByBusinessOwnerEmail(email: String, filters: Iterable<Filter> = emptySet()): List<Order>
    suspend fun getMostExpensiveByBusinessOwnerEmail(email: String): Order?
}
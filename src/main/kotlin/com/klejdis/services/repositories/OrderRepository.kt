package com.klejdis.services.repositories

import com.klejdis.services.filters.Filter
import com.klejdis.services.filters.OrderFilterTransformer
import com.klejdis.services.model.Order

interface OrderRepository : CrudRepository<Order> {
    suspend fun getByBusinessId(businessId: Int): List<Order>
    suspend fun getByBusinessOwnerEmail(email: String, filters: Iterable<Filter> = emptySet()): List<Order>
    suspend fun getByIdAndBusinessOwnerEmail(id: Int, email: String): Order?
    suspend fun getMostExpensiveByBusinessOwnerEmail(email: String): Order?
}
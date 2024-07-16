package com.klejdis.services.repositories

import com.klejdis.services.filters.Filter
import com.klejdis.services.model.Item

interface ItemRepository : CrudRepository<Int, Item> {
    suspend fun getBatch(ids: List<Int>): List<Item>
    suspend fun getByBusinessId(businessId: Int, filters: Iterable<Filter> = emptyList()): List<Item>
    suspend fun getByImageUrl(imageUrl: String): Item?
    suspend fun getByBusinessOwnerEmail(email: String, filters: Iterable<Filter> = emptyList()): List<Item>
}
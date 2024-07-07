package com.klejdis.services.repositories

import com.klejdis.services.model.Item

interface ItemRepository : CrudRepository<Int, Item> {
    suspend fun getBatch(ids: List<Int>): List<Item>
    suspend fun getByBusinessId(businessId: Int): List<Item>
    suspend fun getByBusinessOwnerEmail(email: String): List<Item>
}
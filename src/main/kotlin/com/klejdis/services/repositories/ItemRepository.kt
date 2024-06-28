package com.klejdis.services.repositories

import com.klejdis.services.model.Item

interface ItemRepository : CrudRepository<Item> {
    suspend fun getByBusinessId(businessId: Int): List<Item>
    suspend fun getByBusinessOwnerEmail(email: String): List<Item>
}
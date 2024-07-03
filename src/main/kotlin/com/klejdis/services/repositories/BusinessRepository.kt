package com.klejdis.services.repositories

import com.klejdis.services.model.Business

interface BusinessRepository : CrudRepository<Int, Business> {
    suspend fun getByEmail(email: String): Business?
}
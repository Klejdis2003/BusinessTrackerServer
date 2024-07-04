package com.klejdis.services.repositories

import com.klejdis.services.filters.Filter
import com.klejdis.services.model.Business
import com.klejdis.services.model.Businesses
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.postgresql.util.PSQLException

class BusinessRepositoryKtorm(
    private val database: Database
) : BusinessRepository {
    override suspend fun getByEmail(email: String): Business? {
        return database
            .from(Businesses)
            .select()
            .where { Businesses.ownerEmail eq email }
            .map { row -> Businesses.createEntity(row) }
            .firstOrNull()
    }

    override suspend fun getAll(filters: Iterable<Filter>): List<Business> {
        return database
            .from(Businesses)
            .select()
            .map { row -> Businesses.createEntity(row) }
    }

    override suspend fun get(id: Int): Business? {
        return database
            .from(Businesses)
            .select()
            .where { Businesses.id eq id }
            .map { row -> Businesses.createEntity(row) }
            .firstOrNull()
    }

    @Throws(PSQLException::class)
    override suspend fun create(entity: Business): Business {
        val id = database.insertAndGenerateKey(Businesses) {
            set(it.ownerEmail, entity.ownerEmail)
        } as Int
        entity.id = id
        return entity
    }

    override suspend fun update(entity: Business): Business {
        return Business()
    }

    override suspend fun delete(id: Int): Boolean {
        return false
    }

}
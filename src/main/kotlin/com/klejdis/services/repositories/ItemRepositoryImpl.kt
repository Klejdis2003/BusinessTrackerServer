package com.klejdis.services.repositories

import com.klejdis.services.config.items
import com.klejdis.services.model.Businesses
import com.klejdis.services.model.Item
import com.klejdis.services.model.Items
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.add

class ItemRepositoryImpl(
    private val database: Database
) : ItemRepository {

    override suspend fun get(id: Int): Item? {
        return database
            .from(Items)
            .select()
            .where { Items.id eq id }
            .map { Items.createEntity(it) }
            .firstOrNull()
    }

    override suspend fun getByBusinessId(businessId: Int): List<Item> {
        return database
            .from(Items)
            .select()
            .where { Items.businessId eq businessId }
            .map { Items.createEntity(it) }
    }

    override suspend fun getByBusinessOwnerEmail(email: String): List<Item> {
        return database
            .from(Items)
            .innerJoin(Businesses, on = Items.businessId eq Businesses.id)
            .select()
            .where { Businesses.ownerEmail eq email }
            .map { Items.createEntity(it) }
    }

    override suspend fun create(entity: Item): Item {
        val id = database.items.add(entity)
        entity.id = id
        return entity
    }

    override suspend fun update(entity: Item): Item {
        return Item()
    }

    override suspend fun delete(id: Int): Boolean {
        return database.delete(Items) { it.id eq id } > 0
    }
}
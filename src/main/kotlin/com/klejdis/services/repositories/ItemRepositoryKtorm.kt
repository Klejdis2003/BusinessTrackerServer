package com.klejdis.services.repositories

import com.klejdis.services.config.items
import com.klejdis.services.filters.Filter
import com.klejdis.services.model.Businesses
import com.klejdis.services.model.Item
import com.klejdis.services.model.Items
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.add
import org.ktorm.entity.update
import org.ktorm.schema.ColumnDeclaring

class ItemRepositoryKtorm(
    private val database: Database
) : ItemRepository {

    private fun buildJoinedTablesQuery(
        conditions: List<() -> ColumnDeclaring<Boolean>> = emptyList(),
        aggregateConditions: List<ColumnDeclaring<Boolean>> = emptyList()
    ): Query {
        return database
            .from(Items)
            .innerJoin(Businesses, on = Items.businessId eq Businesses.id)
            .select(
                Items.columns +
                        Businesses.columns
            )
            .whereWithConditions { conditions.forEach { condition -> it += condition() } }
    }

    private fun fetchQuery(query: Query): List<Item> {
        return query.map { Items.createEntity(it) }
    }

    private fun fetchMainQueryWithConditions(conditions: List<() -> ColumnDeclaring<Boolean>> = emptyList()) =
        fetchQuery(buildJoinedTablesQuery(conditions))

    private fun fetchMainQueryWithCondition(condition: () -> ColumnDeclaring<Boolean>) =
        fetchMainQueryWithConditions(listOf(condition))

    override suspend fun get(id: Int): Item? {
        return fetchMainQueryWithCondition { Items.id eq id }.firstOrNull()
    }

    override suspend fun getBatch(ids: List<Int>): List<Item> {
        return fetchMainQueryWithCondition { Items.id inList ids }
    }

    override suspend fun getByBusinessId(businessId: Int): List<Item> {
        return fetchMainQueryWithCondition { Items.businessId eq businessId }
    }

    override suspend fun getByBusinessOwnerEmail(email: String): List<Item> {
        return fetchMainQueryWithConditions(listOf { Businesses.ownerEmail eq email })
    }

    override suspend fun getAll(filters: Iterable<Filter>): List<Item> {
        return fetchMainQueryWithConditions()
    }

    override suspend fun create(entity: Item): Item {
        val id = database.items.add(entity)
        entity.id = id
        return entity
    }

    override suspend fun update(entity: Item): Item {
        database.items.update(entity)
        return entity
    }

    override suspend fun delete(id: Int): Boolean {
        return database.delete(Items) { it.id eq id } > 0
    }
}
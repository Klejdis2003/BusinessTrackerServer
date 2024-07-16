package com.klejdis.services.repositories

import com.klejdis.services.config.items
import com.klejdis.services.filters.Filter
import com.klejdis.services.filters.ItemFilterTransformer
import com.klejdis.services.model.Businesses
import com.klejdis.services.model.Item
import com.klejdis.services.model.ItemTypes
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
    ): Query {
        return database
            .from(Items)
            .innerJoin(Businesses, on = Items.businessId eq Businesses.id)
            .innerJoin(ItemTypes, on = Items.type eq ItemTypes.id)
            .select()
            .whereWithConditions { conditions.forEach { condition -> it += condition() } }
    }

    private fun fetchQuery(query: Query): List<Item> {
        return query.map { Items.createEntity(it) }
    }

    private fun fetchMainQueryWithConditions(
        filters: Iterable<Filter> = emptyList(),
        conditions: List<() -> ColumnDeclaring<Boolean>> = emptyList()) =
        fetchQuery(buildJoinedTablesQuery(
            conditions + ItemFilterTransformer.generateTransformedFilters(filters)
        ))

    private fun fetchMainQueryWithCondition(condition: () -> ColumnDeclaring<Boolean>) =
        fetchMainQueryWithConditions(conditions = listOf(condition))

    override suspend fun get(id: Int): Item? {
        return fetchMainQueryWithCondition { Items.id eq id }.firstOrNull()
    }

    override suspend fun getBatch(ids: List<Int>): List<Item> {
        return fetchMainQueryWithCondition { Items.id inList ids }
    }

    override suspend fun getByBusinessId(businessId: Int, filters: Iterable<Filter>): List<Item> {
        return fetchMainQueryWithConditions(filters = filters, conditions = listOf { Items.businessId eq businessId })
    }

    override suspend fun getByImageUrl(imageUrl: String): Item? {
        return fetchMainQueryWithCondition { Items.imageFilename eq imageUrl }.firstOrNull()
    }

    override suspend fun getByBusinessOwnerEmail(email: String, filters: Iterable<Filter>): List<Item> {
        return fetchMainQueryWithConditions(filters = filters, conditions = listOf { Businesses.ownerEmail eq email })
    }

    override suspend fun getAll(filters: Iterable<Filter>): List<Item> {
        return fetchMainQueryWithConditions()
    }

    override suspend fun create(entity: Item): Item {
        database.items.add(entity)
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
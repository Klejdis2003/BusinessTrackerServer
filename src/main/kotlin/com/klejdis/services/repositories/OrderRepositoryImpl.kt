package com.klejdis.services.repositories


import com.klejdis.services.config.orders
import com.klejdis.services.model.*
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.add
import org.ktorm.entity.update
import org.ktorm.schema.ColumnDeclaring

class OrderRepositoryImpl(
    private val database: Database
): OrderRepository {
    private fun fetchJoinedTables() : Query {
        return database
            .from(Orders)
            .innerJoin(OrderItems, on = OrderItems.orderId eq Orders.id)
            .innerJoin(Items, on = OrderItems.itemId eq Items.id)
            .innerJoin(Businesses, on = Orders.businessId eq Businesses.id)
            .innerJoin(ItemTypes, on = Items.type eq ItemTypes.id)
            .select()
    }
    private fun fetchJoinedTablesWithConditions(
        conditions: List<() -> ColumnDeclaring<Boolean>>
    ): List<Order>{
        val orderIdItemsMap = mutableMapOf<Int, MutableList<Item>>()
        val orderMap = mutableMapOf<Int, Order>()
        fetchJoinedTables()
            .whereWithConditions { conditions.forEach { condition ->
                it += condition()
            } }
            .forEach {
                val item = Items.createEntity(it)
                val order = Orders.createEntity(it).apply { business = item.business }
                orderIdItemsMap.getOrPut(order.id) { mutableListOf() }.add(item)
                orderMap.putIfAbsent(order.id, order)
            }
        return orderMap.map { (_, order) ->
            order.apply { items = orderIdItemsMap[order.id] ?: emptyList() } }
    }

    private fun fetchJoinedTablesWithCondition(
        condition: () -> ColumnDeclaring<Boolean>
    ): List<Order>{
        return fetchJoinedTablesWithConditions(listOf(condition))
    }

    override suspend fun getByBusinessId(businessId: Int): List<Order> {
        return fetchJoinedTablesWithCondition { Orders.businessId eq businessId }
    }

    override suspend fun getByBusinessOwnerEmail(email: String): List<Order> {
        return fetchJoinedTablesWithCondition { Businesses.ownerEmail eq email }
    }

    override suspend fun getByIdAndBusinessOwnerEmail(id: Int, email: String): Order? {
        val conditions = listOf(
            { Orders.id eq id },
            { Businesses.ownerEmail eq email }
        )
        return fetchJoinedTablesWithConditions(conditions).firstOrNull() //it is known that there is only one or zero orders with a given id
    }

    override suspend fun get(id: Int): Order? {
        return fetchJoinedTablesWithCondition { Orders.id eq id }.firstOrNull() //it is known that there is only one or zero orders with a given id
    }

    override suspend fun create(entity: Order): Order {
        val id = database.orders.add(entity)
        entity.id = id
        return entity
    }

    override suspend fun update(entity: Order): Order {
        database.orders.update(entity)
        return entity
    }

    override suspend fun delete(id: Int): Boolean {
        return database.delete(Orders){ it.id eq id } > 0
    }
}
package com.klejdis.services.repositories


import com.klejdis.services.config.orders
import com.klejdis.services.filters.OrderFilterTransformer
import com.klejdis.services.model.*
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.add
import org.ktorm.entity.update
import org.ktorm.schema.ColumnDeclaring

class OrderRepositoryImpl(
    private val database: Database,
    private val orderFilterTransformer: OrderFilterTransformer
) : OrderRepository {
    private fun fetchJoinedTables(): Query {
        return database
            .from(Orders)
            .innerJoin(OrderItems, on = OrderItems.orderId eq Orders.id)
            .innerJoin(Items, on = OrderItems.itemId eq Items.id)
            .innerJoin(Customers, on = Orders.customerPhone eq Customers.phone)
            .innerJoin(Businesses, on = Orders.businessId eq Businesses.id)
            .innerJoin(ItemTypes, on = Items.type eq ItemTypes.id)
            .select()

    }

    private fun fetchJoinedTablesWithConditions(
        conditions: List<() -> ColumnDeclaring<Boolean>>
    ): List<Order> {
        val orderIdItemsMap = mutableMapOf<Int, MutableList<OrderItem>>()
        val orderMap = mutableMapOf<Int, Order>()
        fetchJoinedTables()
            .whereWithConditions {
                conditions.forEach { condition ->
                    it += condition()
                }
            }
            .forEach {
                val item = Items.createEntity(it)
                val order = Orders.createEntity(it).apply {
                    business = item.business
                }
                orderIdItemsMap
                    .getOrPut(order.id) { mutableListOf() }
                    .add(OrderItem(item, it[OrderItems.quantity] as Int))

                orderMap.getOrPut(order.id) { order }
                    .total += item.price * (it[OrderItems.quantity] as Int)
            }
        return orderMap.map { (_, order) ->
            order.apply { items = orderIdItemsMap[order.id] ?: emptyList() }
        }
    }

    private fun fetchJoinedTablesWithCondition(
        condition: () -> ColumnDeclaring<Boolean>
    ): List<Order> {
        return fetchJoinedTablesWithConditions(listOf(condition))
    }

    override suspend fun getByBusinessId(businessId: Int): List<Order> {
        return fetchJoinedTablesWithCondition { Orders.businessId eq businessId }
    }

    override suspend fun getByBusinessOwnerEmail(email: String, filters: Map<String, String>): List<Order> {
        val transformedFilters = orderFilterTransformer.generateTransformedFilters(filters)
                    as MutableList<() -> ColumnDeclaring<Boolean>>
        transformedFilters.add { Businesses.ownerEmail eq email }
        return fetchJoinedTablesWithConditions(transformedFilters)
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
        val affectedRecords = database.orders.add(entity)
        if (affectedRecords == 0) throw Exception("Failed to create order")
        database.batchInsert(OrderItems) {
            entity.items.forEach { orderItem ->
                item {
                    set(it.itemId, orderItem.item.id)
                    set(it.orderId, entity.id)
                }
            }
        }
        return get(entity.id)!!
    }

    override suspend fun update(entity: Order): Order {
        database.orders.update(entity)
        return entity
    }

    override suspend fun delete(id: Int): Boolean {
        return database.delete(Orders) { it.id eq id } > 0
    }
}
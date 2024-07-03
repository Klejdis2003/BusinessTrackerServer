package com.klejdis.services.repositories
import com.klejdis.services.config.orders
import com.klejdis.services.filters.Filter
import com.klejdis.services.filters.OrderFilterTransformer
import com.klejdis.services.model.*
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.add
import org.ktorm.entity.update
import org.ktorm.schema.ColumnDeclaring

class OrderRepositoryImpl(
    private val database: Database
) : OrderRepository {
    private fun buildJoinedTablesQuery(
        conditions: List<() -> ColumnDeclaring<Boolean>>
    ): Query {
        return database
            .from(Orders)
            .innerJoin(OrderItems, on = OrderItems.orderId eq Orders.id)
            .innerJoin(Items, on = OrderItems.itemId eq Items.id)
            .innerJoin(Customers, on = Orders.customerPhone eq Customers.phone)
            .innerJoin(Businesses, on = Orders.businessId eq Businesses.id)
            .innerJoin(ItemTypes, on = Items.type eq ItemTypes.id)
            .select()
            .whereWithConditions { conditions.forEach { condition -> it += condition() } }
    }


    private fun fetchQuery(query: Query): List<Order> {
        val orderIdItemsMap = mutableMapOf<Int, MutableList<OrderItem>>()
        val orderMap = mutableMapOf<Int, Order>()
        query.forEach {
            val item = Items.createEntity(it)
            val order = Orders.createEntity(it).apply {
                business = item.business
            }
            orderIdItemsMap
                .getOrPut(order.id) { mutableListOf() }
                .add(OrderItem(item, it[OrderItems.quantity] as Int))

            orderMap.getOrPut(order.id) { order }
        }
        return orderMap.map { (_, order) ->
            order.apply { items = orderIdItemsMap[order.id] ?: emptyList() }
        }
    }

    private fun fetchJoinedTablesWithConditions(
        filters: Iterable<Filter> = emptyList(),
        additionalConditions: List<() -> ColumnDeclaring<Boolean>> = emptyList()
    ): List<Order> {
        val conditions = OrderFilterTransformer.generateTransformedFilters(filters) + additionalConditions
        return fetchQuery(buildJoinedTablesQuery(conditions))
    }

    private fun fetchJoinedTablesWithCondition(
        filters: Iterable<Filter> = emptyList(),
        condition: () -> ColumnDeclaring<Boolean>
    ): List<Order> =
        fetchJoinedTablesWithConditions(filters, additionalConditions = listOf(condition))


    override suspend fun filterByBusinessId(businessId: Int): List<Order> {
        return fetchJoinedTablesWithCondition { Orders.businessId eq businessId }
    }

    override suspend fun filterByBusinessOwnerEmail(
        email: String,
        filters: Iterable<Filter>
    ): List<Order> {
        return fetchJoinedTablesWithCondition(filters) {
            Businesses.ownerEmail eq email
        }
    }

    override suspend fun getMostExpensiveByBusinessOwnerEmail(email: String): Order? {
        val maxOrderPrice = database
            .from(Orders)
            .innerJoin(Businesses, on = Orders.businessId eq Businesses.id)
            .select(max(Orders.total))
            .where { Businesses.ownerEmail eq email }
            .map { it.getInt(1) }
            .firstOrNull() ?: return null

        val maxOrderId = database
            .from(Orders)
            .select(Orders.id)
            .where { Orders.total eq maxOrderPrice }
            .map { it.getInt(1) }
            .firstOrNull() ?: return null


        return get(maxOrderId)
    }

    override suspend fun getAll(filters: Iterable<Filter>): List<Order> {
        return fetchJoinedTablesWithConditions(filters)
    }

    override suspend fun get(id: Int): Order? {
        return fetchJoinedTablesWithCondition { Orders.id eq id }.firstOrNull()
    }

    override suspend fun create(entity: Order): Order {
        entity.total = entity.items.sumOf { it.item.price * it.quantity }
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
        entity.total = entity.items.sumOf { it.item.price * it.quantity }
        database.orders.update(entity)
        return entity
    }

    override suspend fun delete(id: Int): Boolean {
        return database.delete(Orders) { it.id eq id } > 0
    }
}
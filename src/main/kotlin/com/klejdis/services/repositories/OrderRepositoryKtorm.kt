package com.klejdis.services.repositories
import com.klejdis.services.config.orders
import com.klejdis.services.filters.Filter
import com.klejdis.services.filters.FilterType
import com.klejdis.services.filters.KtormFilter
import com.klejdis.services.filters.OrderFilterTransformer
import com.klejdis.services.model.*
import org.ktorm.database.Database
import org.ktorm.database.asIterable
import org.ktorm.dsl.*
import org.ktorm.entity.add
import org.ktorm.entity.update
import org.ktorm.schema.ColumnDeclaring
import java.sql.ResultSet

class OrderRepositoryKtorm(
    private val database: Database
) : OrderRepository {

    private fun buildJoinedTablesQuery(
        conditions: List<() -> ColumnDeclaring<Boolean>>,
        aggregateConditions: List<ColumnDeclaring<Boolean>> = emptyList()
    ): Query {
        var query = database
            .from(Orders)
            .innerJoin(OrderItems, on = OrderItems.orderId eq Orders.id)
            .innerJoin(Items, on = OrderItems.itemId eq Items.id)
            .innerJoin(Customers, on = Orders.customerPhone eq Customers.phone)
            .innerJoin(Businesses, on = Orders.businessId eq Businesses.id)
            .innerJoin(ItemTypes, on = Items.type eq ItemTypes.id)
            .select()
            .whereWithConditions { conditions.forEach { condition -> it += condition() } }
        if(aggregateConditions.isNotEmpty()) {
            query = query.having {
                aggregateConditions.reduce { acc, columnDeclaring -> acc and columnDeclaring }
            }
        }
        return query
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
        additionalConditions: List<() -> ColumnDeclaring<Boolean>> = emptyList(),
    ): List<Order> {
        val conditions = OrderFilterTransformer.generateTransformedFilters(filters) as
                MutableList<KtormFilter>
        val aggregateConditions = mutableListOf<ColumnDeclaring<Boolean>>()
        conditions.forEach { filter ->
            if (filter.type == FilterType.AGGREGATE) {
                aggregateConditions.add(filter.condition())
            }
        }
        conditions.removeAll { it.type == FilterType.AGGREGATE }
        return fetchQuery(buildJoinedTablesQuery(
            conditions.map { it.condition } + additionalConditions,
            aggregateConditions
        ))
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
        val mostExpensiveOrderId = database.useConnection { conn ->
            val sql = """
                WITH
                    order_totals AS (
                    SELECT orders.id, SUM(order_items.quantity * items.price) AS total
                    FROM orders
                    INNER JOIN order_items ON orders.id = order_items.order_id
                    INNER JOIN items ON order_items.item_id = items.id
                    INNER JOIN businesses ON orders.business_id = businesses.id
                    INNER JOIN customers ON orders.customer_phone = customers.phone
                    WHERE businesses.owner_email = ?
                    GROUP BY orders.id, businesses.id, customers.phone
                )
                SELECT order_totals.*
                FROM order_totals
                WHERE order_totals.total = (SELECT MAX(total) FROM order_totals)
                """.trimIndent()
            conn.prepareStatement(sql).use{
                it.setString(1, email)
                it.executeQuery().apply{next()}.getInt("id")
            }
        }
        return get(mostExpensiveOrderId)
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
                    set(it.quantity, orderItem.quantity)
                }
            }
        }
        return entity
    }

    override suspend fun update(entity: Order): Order {
        entity.total = entity.items.sumOf { it.item.price * it.quantity }
        database.orders.update(entity)
        return get(entity.id)!!
    }

    override suspend fun delete(id: Int): Boolean {
        return database.delete(Orders) { it.id eq id } > 0
    }
}
package com.klejdis.services.repositories


import com.klejdis.services.model.*
import org.ktorm.database.Database
import org.ktorm.dsl.*

class OrderRepositoryImpl(
    private val database: Database
): OrderRepository {
    override suspend fun get(id: Int): Order? {
        val items = mutableListOf<Item>()
        val order = database
            .from(Orders)
            .innerJoin(OrderItems, on = OrderItems.orderId eq Orders.id)
            .innerJoin(Items, on = OrderItems.itemId eq Items.id)
            .innerJoin(Businesses, on = Orders.businessId eq Businesses.id)
            .select(Orders.columns + Items.columns + Businesses.columns)
            .where(Orders.id eq id)
            .map {
                val item = Items.createEntity(it)
                item.business = Businesses.createEntity(it)
                items.add(item)
                Orders.createEntity(it).apply { business = item.business}
            }
            .firstOrNull()
        order?.items = items
        return order
    }

    override suspend fun create(entity: Order): Order {
        TODO("Not yet implemented")
    }

    override suspend fun update(id: Int): Order {
        TODO("Not yet implemented")
    }

    override suspend fun delete(id: Int): Boolean {
        TODO("Not yet implemented")
    }
}
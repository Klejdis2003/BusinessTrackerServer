package com.klejdis.services.model

import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.date
import org.ktorm.schema.int
import org.ktorm.schema.varchar
import java.time.LocalDate

interface Order : Entity<Order> {
    companion object : Entity.Factory<Order>()
    var id: Int
    var business: Business
    var customer: Customer
    var date: LocalDate
    var items: List<OrderItem>
    var total: Int

}

object Orders : Table<Order>("orders") {
    val id = int("id").primaryKey().bindTo { it.id }
    val businessId = int("business_id").references(Businesses) { it.business }
    val customerPhone = varchar("customer_phone").references(Customers) { it.customer }
    val date = date("date").bindTo { it.date }
    val total = int("total").bindTo { it.total }
}

data class OrderItem(
    val item: Item,
    val quantity: Int
)

object OrderItems : Table<Nothing>("order_items") {
    val id = int("id").primaryKey()
    val orderId = int("order_id")
    val itemId = int("item_id")
    val quantity = int("quantity")
}
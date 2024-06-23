package com.klejdis.services.model

import kotlinx.serialization.Serializable
import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.date
import org.ktorm.schema.double
import org.ktorm.schema.int
import java.time.LocalDate
import java.util.Date

interface Order: Entity<Order> {
    companion object: Entity.Factory<Order>()
    var id: Int
    var business: Business
    var date: LocalDate
    var items: List<Item>
}



object Orders: Table<Order>("orders") {
    val id = int("id").primaryKey().bindTo { it.id }
    val businessId = int("business_id").references(Businesses) { it.business }
    val date = date("date").bindTo { it.date }
}

object OrderItems: Table<Nothing>("order_items") {
    val orderId = int("order_id")
    val itemId = int("item_id")
    val quantity = int("quantity")
}
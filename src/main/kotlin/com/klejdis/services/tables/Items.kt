package com.klejdis.services.tables

import com.klejdis.services.model.Item
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar

object Items: Table<Item>("items") {
    val id = int("id").primaryKey().bindTo { it.id }
    val name = varchar("name").bindTo { it.name }
    val description = varchar("description").bindTo { it.type.description }
    val quantity = int("quantity").bindTo { it.quantity }
    val purchasePrice = int("purchase_price").bindTo { it.purchasePrice }
    val price = int("price").bindTo { it.price }
    val type = int("type").references(ItemTypes) { it.type }
}
package com.klejdis.services.model

import com.klejdis.services.dto.ItemDto
import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar


/**
 * Represents an item that can be sold by a business.
 * @property id the unique identifier of the item.
 * @property name the name of the item.
 * @property business the business that owns the item.
 * @property purchasePrice the price at which the business bought the item.
 * @property price the price at which the business sells the item.
 * @property quantity the number of items in stock.
 * @property type the type of the item.
 */
interface Item: Entity<Item> {
    companion object: Entity.Factory<Item>()
    var id: Int
    var name: String
    var business: Business
    var purchasePrice: Int
    var price: Int
    var type: ItemType
}

object Items: Table<Item>("items") {
    val id = int("id").primaryKey().bindTo { it.id }
    val businessId = int("business_id").references(Businesses) { it.business }
    val name = varchar("name").bindTo { it.name }
    val description = varchar("description").bindTo { it.type.description }
    val purchasePrice = int("purchase_price").bindTo { it.purchasePrice }
    val price = int("price").bindTo { it.price }
    val type = int("item_type_id").references(ItemTypes) { it.type }
}
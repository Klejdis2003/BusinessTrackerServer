package com.klejdis.services.model

import org.ktorm.entity.Entity

interface Item: Entity<Item> {
    companion object: Entity.Factory<Item>()
    val id: Int
    var name: String
    var purchasePrice: Int
    var price: Int
    var quantity: Int
    var type: ItemType
}

fun main() {
    val item = Item {
        name = "Item 1"
        purchasePrice = 100
        price = 150
        quantity = 10
        type = ItemType {
            name = "Type 1"
            description = "Description 1"
        }
    }
    println(item)
}

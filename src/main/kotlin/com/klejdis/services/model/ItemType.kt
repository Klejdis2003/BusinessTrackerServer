package com.klejdis.services.model

import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar

interface ItemType : Entity<ItemType> {
    companion object : Entity.Factory<ItemType>()

    var id: Int
    var name: String
    var description: String
}


object ItemTypes : Table<ItemType>(tableName = "item_types") {
    val id = int("id").primaryKey().bindTo { it.id }
    val name = varchar("name").bindTo { it.name }
    val description = varchar("description").bindTo { it.description }
}


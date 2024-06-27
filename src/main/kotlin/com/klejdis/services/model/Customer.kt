package com.klejdis.services.model

import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar

interface Customer: Entity<Customer> {
    companion object : Entity.Factory<Customer>()
    var id: Int
    var name: String
    var phone : String
}

object Customers: Table<Customer>("customers") {
    val id = int("id").primaryKey().bindTo { it.id }
    val name = varchar("name").bindTo { it.name }
    val phone = varchar("phone").bindTo { it.phone }
}

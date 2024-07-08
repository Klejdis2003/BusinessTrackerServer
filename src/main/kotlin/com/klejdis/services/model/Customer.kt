package com.klejdis.services.model

import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.varchar

interface Customer : Entity<Customer> {
    companion object : Entity.Factory<Customer>()
    var phone: String
    var name: String

}

object Customers : Table<Customer>("customers") {
    val phone = varchar("phone").primaryKey().bindTo { it.phone }
    val name = varchar("name").bindTo { it.name }

}

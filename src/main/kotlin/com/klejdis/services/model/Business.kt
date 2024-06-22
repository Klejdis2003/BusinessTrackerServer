package com.klejdis.services.model

import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar

interface Business: Entity<Business> {
    companion object : Entity.Factory<Business>()
    var id: Int
    var ownerEmail: String
}

object Businesses: Table<Business>("businesses") {
    val id = int("id").primaryKey().bindTo { it.id }
    val ownerEmail = varchar("owner_email").bindTo { it.ownerEmail }
}
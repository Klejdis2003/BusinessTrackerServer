package com.klejdis.services.model

import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.date
import org.ktorm.schema.int
import org.ktorm.schema.varchar
import java.time.LocalDate

interface Expense: Entity<Expense> {
    companion object: Entity.Factory<Expense>()
    var id: Int
    var amount: Int
    var currency: String
    var date: LocalDate
    var comment : String
    var business: Business
}

object Expenses: Table<Expense>("expenses") {
    val id = int("id").primaryKey().bindTo { it.id }
    val amount = int("amount").bindTo { it.amount }
    val businessId = int("business_id").references(Businesses) { it.business }
    val currency = varchar("currency").bindTo { it.currency }
    val date = date("date").bindTo { it.date }
}
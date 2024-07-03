package com.klejdis.services.model

import kotlinx.serialization.Serializable
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

/**
 * Represents an expense incurred by a business.
 * @property id the unique identifier of the expense.
 * @property amount the amount of money spent.
 * @property businessId the id of the business that incurred the expense.
 * @property currency the currency in which the expense was incurred.
 * @property date the date on which the expense was incurred.
 * @property comment any additional comments or notes about the expense.
 */
object Expenses: Table<Expense>("expenses") {
    val id = int("id").primaryKey().bindTo { it.id }
    val amount = int("amount").bindTo { it.amount }
    val businessId = int("business_id").references(Businesses) { it.business }
    val currency = varchar("currency").bindTo { it.currency }
    val date = date("date").bindTo { it.date }
    val comment = varchar("comment").bindTo { it.comment }
}
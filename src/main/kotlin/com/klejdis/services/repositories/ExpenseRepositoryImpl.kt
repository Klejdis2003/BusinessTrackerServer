package com.klejdis.services.repositories

import com.klejdis.services.filters.ExpenseFilterTransformer
import com.klejdis.services.filters.Filter
import com.klejdis.services.model.Businesses
import com.klejdis.services.model.Expense
import com.klejdis.services.model.Expenses
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.schema.ColumnDeclaring

class ExpenseRepositoryImpl(
    private val database: Database
): ExpenseRepository{
    private fun buildJoinedTablesQuery(
        conditions: Iterable<() -> ColumnDeclaring<Boolean>>
    ): Query {
        return database
            .from(Expenses)
            .innerJoin(Businesses, on = Businesses.id eq Expenses.businessId)
            .select()
            .whereWithConditions { conditions.forEach { condition -> it += condition() } }
    }

    override suspend fun getAll(filters: Iterable<Filter>): List<Expense> {
        val filters = ExpenseFilterTransformer.generateTransformedFilters(filters)
        return buildJoinedTablesQuery(filters)
            .map { Expenses.createEntity(it) }
    }
    override suspend fun get(id: Int): Expense? {
        TODO("Not yet implemented")
  }

    override suspend fun create(entity: Expense): Expense {
        TODO("Not yet implemented")
    }

    override suspend fun update(entity: Expense): Expense {
        TODO("Not yet implemented")
    }

    override suspend fun delete(id: Int): Boolean {
        TODO("Not yet implemented")
    }
}
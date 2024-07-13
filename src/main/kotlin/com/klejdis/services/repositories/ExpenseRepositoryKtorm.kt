package com.klejdis.services.repositories

import com.klejdis.services.config.expenses
import com.klejdis.services.filters.ExpenseFilterTransformer
import com.klejdis.services.filters.Filter
import com.klejdis.services.model.Businesses
import com.klejdis.services.model.Expense
import com.klejdis.services.model.Expenses
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.add
import org.ktorm.entity.update
import org.ktorm.schema.ColumnDeclaring

class ExpenseRepositoryKtorm(
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

    private fun fetchQuery(query: Query): List<Expense> {
        return query.map { Expenses.createEntity(it) }
    }
    private fun fetchMainQueryWithConditions(
        filters: Iterable<Filter> = emptyList(),
        additionalConditions: List<() -> ColumnDeclaring<Boolean>> = emptyList()
    ): List<Expense> {
        val allConditions = ExpenseFilterTransformer.generateTransformedFilters(filters) + additionalConditions
        return fetchQuery(buildJoinedTablesQuery(allConditions))
    }
    private fun fetchMainQueryWithCondition(condition: () -> ColumnDeclaring<Boolean>): List<Expense> {
        return fetchMainQueryWithConditions(additionalConditions = listOf(condition))
    }

    override suspend fun filterByBusinessOwnerEmail(email: String, filters: Iterable<Filter>): List<Expense> {
        return fetchMainQueryWithConditions(filters, additionalConditions = listOf { Businesses.ownerEmail eq email })
    }

    override suspend fun getAll(filters: Iterable<Filter>): List<Expense> {
        return fetchMainQueryWithConditions(filters)
    }
    override suspend fun get(id: Int): Expense? {
        return fetchMainQueryWithCondition { Expenses.id eq id }.firstOrNull()
  }

    override suspend fun create(entity: Expense): Expense {
        database.expenses.add(entity)
        return entity
    }

    override suspend fun update(entity: Expense): Expense {
        database.expenses.update(entity)
        return entity
    }

    override suspend fun delete(id: Int): Boolean {
        return database.delete(Expenses) { Expenses.id eq id } > 0
    }
}
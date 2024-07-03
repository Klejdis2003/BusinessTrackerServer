package com.klejdis.services.repositories

import com.klejdis.services.filters.Filter
import com.klejdis.services.model.Expense
import org.ktorm.schema.ColumnDeclaring

interface ExpenseRepository: CrudRepository<Expense> {
    suspend fun getAll(filters: Iterable<Filter> = emptyList()): List<Expense>
}
package com.klejdis.services.repositories

import com.klejdis.services.filters.Filter
import com.klejdis.services.model.Expense

interface ExpenseRepository: CrudRepository<Int, Expense> {
    suspend fun filterByBusinessOwnerEmail(email: String, filters: Iterable<Filter> = emptySet()): List<Expense>
}
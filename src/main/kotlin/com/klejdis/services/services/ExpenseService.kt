package com.klejdis.services.services

import com.klejdis.services.dto.ExpenseDto
import com.klejdis.services.dto.ExpenseMapper
import com.klejdis.services.filters.ExpenseFilterType
import com.klejdis.services.filters.Filter
import com.klejdis.services.repositories.BusinessRepository
import com.klejdis.services.repositories.ExpenseRepository

class ExpenseService(
    private val expenseRepository: ExpenseRepository,
    private val businessRepository: BusinessRepository
) {
    suspend fun getAll(businessOwnerEmail: String, filters: List<Filter>): List<ExpenseDto> {
        val filters = filters as MutableList<Filter>
        filters.add ( Filter(
            ExpenseFilterType.BusinessOwnerEmail.typeName,
            businessOwnerEmail
        ) )
        return expenseRepository.getAll(filters).map { ExpenseMapper.toDto(it) }
    }
}
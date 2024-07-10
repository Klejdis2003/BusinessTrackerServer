package com.klejdis.services.services

import com.klejdis.services.dto.ExpenseCreationDto
import com.klejdis.services.dto.ExpenseDto
import com.klejdis.services.filters.ExpenseFilterCategory
import com.klejdis.services.filters.Filter
import com.klejdis.services.model.Expense
import com.klejdis.services.repositories.BusinessRepository
import com.klejdis.services.repositories.ExpenseRepository

class ExpenseService(
    private val expenseRepository: ExpenseRepository,
    private val businessRepository: BusinessRepository,
    loggedInEmail: String
): Service<Expense>("Expense", loggedInEmail = loggedInEmail) {

    private suspend fun getExpenseWithUpdatedBusiness(dto: ExpenseCreationDto, businessOwnerEmail: String): Expense {
        val expense = ExpenseCreationDto.toEntity(dto)
        val business = businessRepository.getByEmail(businessOwnerEmail)
            ?: throw EntityNotFoundException("Business with email=$businessOwnerEmail does not exist")
        return expense.apply { this.business = business }
    }
    /**
     * @param businessOwnerEmail The email of the business owner whose expenses are being fetched
     * @param filters The filters to apply to the expenses
     * @return A list of ExpenseDto objects that match the given filters
     */
    suspend fun getAll(filters: List<Filter>): List<ExpenseDto> {
        val filters = filters as MutableList<Filter>
        filters.add ( Filter(
            ExpenseFilterCategory.BusinessOwnerEmail.typeName,
            loggedInEmail
        ) )
        return expenseRepository.getAll(filters).map { ExpenseDto.fromEntity(it)}
    }

    /**
     * @param id The id of the expense to fetch
     * @return ExpenseDto if the expense exists and belongs to the business owner, null otherwise
     */
    suspend fun get(id: Int): ExpenseDto? {
        val expense = expenseRepository.get(id)
        return expense?.takeIf { it.business.ownerEmail == loggedInEmail}?.let { ExpenseDto.fromEntity(it)}
    }

    /**
     * @param dto The ExpenseDto object to create
     * @return The created ExpenseDto object. [See Possible Errors][executeCreateBlockWithErrorHandling]
     * @see executeCreateBlockWithErrorHandling
     */
    suspend fun create(dto: ExpenseCreationDto): ExpenseDto {
        val expense = getExpenseWithUpdatedBusiness(dto, loggedInEmail)
        val createdExpense = executeCreateBlockWithErrorHandling {
            expenseRepository.create(expense)
        }
        return ExpenseDto.fromEntity(createdExpense)
    }

    /**
     * @param dto The ExpenseDto object to update
     * @param businessOwnerEmail The email of the business owner updating the expense
     * @return The updated ExpenseDto object. [See Possible Errors][executeUpdateBlock]
     * @see executeUpdateBlock
     */
    suspend fun update(dto: ExpenseCreationDto): ExpenseDto {
        val expense = getExpenseWithUpdatedBusiness(dto, loggedInEmail)
        val updatedExpense = executeUpdateBlock {
            expenseRepository.update(expense)
        }
        return ExpenseDto.fromEntity(updatedExpense)
    }

    /**
     * Deletes an expense if it exists and belongs to the business owner
     * @param id The id of the expense to delete
     * @param businessOwnerEmail The email of the business owner deleting the expense
     * @return True if the expense was deleted, false otherwise
     */
    suspend fun delete(id: Int): Boolean {
        val expense = expenseRepository.get(id)
        if (expense?.business?.ownerEmail != loggedInEmail) return false
        return expenseRepository.delete(id)

    }
}
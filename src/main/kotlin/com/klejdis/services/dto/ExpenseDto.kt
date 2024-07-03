package com.klejdis.services.dto

import com.klejdis.services.model.Expense
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class ExpenseDto(
    val id: Int,
    val amount: Int,
    val currency: CurrencyDto,
    val date: String,
    val comment: String,
)

object ExpenseMapper {
    fun toDto(expense: Expense): ExpenseDto {
        return ExpenseDto(
            id = expense.id,
            amount = expense.amount,
            currency = CurrencyMapper.toDto(Currency.getInstance(expense.currency)),
            date = expense.date.toString(),
            comment = expense.comment,
        )
    }
}
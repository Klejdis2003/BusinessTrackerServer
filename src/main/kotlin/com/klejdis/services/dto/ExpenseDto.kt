package com.klejdis.services.dto

import com.klejdis.services.model.Expense
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.util.*

@Serializable
data class ExpenseDto(
    val id: Int,
    val amount: Int,
    val currency: CurrencyDto,
    val date: String,
    val category: String,
    val comment: String
) {
    companion object : Mappable<Expense, ExpenseDto> {
        override fun fromEntity(entity: Expense): ExpenseDto {
            return ExpenseDto(
                id = entity.id,
                amount = entity.amount,
                currency = CurrencyDto.fromCurrency(
                    Currency.getInstance(entity.currencyCode)
                ),
                date = entity.date.toString(),
                category = entity.category,
                comment = entity.comment,
            )
        }

        override fun toEntity(dto: ExpenseDto): Expense {
            return Expense {
                id = dto.id
                amount = dto.amount
                currencyCode = dto.currency.code
                date = LocalDate.parse(dto.date)
                comment = dto.comment
                category = dto.category
            }
        }
    }
}

@Serializable
data class ExpenseCreationDto(
    val amount: Int,
    val currencyCode: String,
    val category: String,
    val comment: String = ""
) {
    companion object : Mappable<Expense, ExpenseCreationDto> {
        override fun fromEntity(entity: Expense): ExpenseCreationDto {
            return ExpenseCreationDto(
                amount = entity.amount,
                currencyCode = entity.currencyCode,
                comment = entity.comment,
                category = entity.category
            )
        }

        override fun toEntity(dto: ExpenseCreationDto): Expense {
            return Expense {
                date = LocalDate.now()
                amount = dto.amount
                currencyCode = dto.currencyCode
                comment = dto.comment
                category = dto.category
            }
        }
    }
}



object ExpenseMapper {
    fun toDto(expense: Expense): ExpenseDto {
        return ExpenseDto(
            id = expense.id,
            amount = expense.amount,
            currency = CurrencyMapper.toDto(Currency.getInstance(expense.currencyCode)),
            date = expense.date.toString(),
            comment = expense.comment,
            category = expense.category
        )
    }
}
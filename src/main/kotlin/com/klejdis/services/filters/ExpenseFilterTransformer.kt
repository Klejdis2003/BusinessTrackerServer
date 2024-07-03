package com.klejdis.services.filters

import com.klejdis.services.model.Businesses
import com.klejdis.services.model.Expenses
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.dsl.greaterEq
import org.ktorm.dsl.lessEq
import org.ktorm.schema.ColumnDeclaring
import java.time.LocalDate

typealias ExpenseFilter = TypeSafeFilter<() -> ColumnDeclaring<Boolean>>


object ExpenseFilterTransformer: KtormFilterTransformer(ExpenseFilterType::class)

sealed class ExpenseFilterType: KtormFilterType() {
    data object BusinessOwnerEmail: ExpenseFilterType() {
        override fun transform(value: String): () -> ColumnDeclaring<Boolean> {
            return { Businesses.ownerEmail eq value }
        }

    }
    data object MaxAmount: ExpenseFilterType() {
        override fun transform(value: String): () -> ColumnDeclaring<Boolean> {
            return { Expenses.amount lessEq value.toInt() }
        }
    }
    data object MinAmount: ExpenseFilterType() {
        override fun transform(value: String): () -> ColumnDeclaring<Boolean> {
            return { Expenses.amount greaterEq value.toInt() }
        }
    }
    data object Date: ExpenseFilterType() {
        override fun transform(value: String): () -> ColumnDeclaring<Boolean> {
            return { Expenses.date eq LocalDate.parse(value) }
        }
    }
    data object EarlierThan: ExpenseFilterType() {
        override fun transform(value: String): () -> ColumnDeclaring<Boolean> {
            return { Expenses.date lessEq LocalDate.parse(value) }
        }
    }
    data object LaterThan: ExpenseFilterType() {
        override fun transform(value: String): () -> ColumnDeclaring<Boolean> {
            return { Expenses.date greaterEq LocalDate.parse(value) }
        }
    }

    data object Month: ExpenseFilterType() {
        override fun transform(value: String): () -> ColumnDeclaring<Boolean> {
            return {
                val date = LocalDate.parse(value)
                val start = date.withDayOfMonth(1)
                val end = date.withDayOfMonth(date.lengthOfMonth())
                (Expenses.date greaterEq start) and (Expenses.date lessEq end)
            }
        }
    }
}
package com.klejdis.services.filters

import com.klejdis.services.model.Businesses
import com.klejdis.services.model.Expenses
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.dsl.greaterEq
import org.ktorm.dsl.lessEq
import org.ktorm.schema.ColumnDeclaring
import java.time.LocalDate
import java.time.format.DateTimeParseException

typealias ExpenseFilter = TypeSafeFilter<() -> ColumnDeclaring<Boolean>>


object ExpenseFilterTransformer: KtormFilterTransformer(ExpenseFilterType::class)

sealed class ExpenseFilterType: KtormFilterType() {

    /**
     * @param filterName the name of the filter, used to construct the error message
     * @param e the exception that was thrown
     * @exception IllegalArgumentException if the value is not a number or a date in the format yyyy-MM-dd
     * @exception DateTimeParseException if the date is not in the format yyyy-MM-dd or if the date is invalid
     */
    private fun handleException(filterName:String, e: Exception): Exception{
        val baseMessage ="Invalid value for $filterName filter."
        when(e){
            is NumberFormatException -> {
                val message = "$baseMessage Expected a number"
                throw IllegalArgumentException(message)
            }
            is DateTimeParseException -> {
                val message = "$baseMessage Expected a date in the format yyyy-MM-dd"
                throw IllegalArgumentException(message)
            }
            else -> throw e
        }
    }

    /**
     * Converts a string to a value of type [to] and handles exceptions by displaying better error messages
     * that pinpoint the error in a way that is more user-friendly, especially for API users.
     * @param filterName the name of the filter
     * @param block the block of code that converts the string to the desired type
     * @return the converted value if successful, [or these exceptions][handleException]
     * @see handleException
     */
    protected fun<to> convertWithExceptionHandling(
        filterName: String,
        block: () -> to
    ): to {
         try {
            return block()
        } catch (e: Exception) {
            throw handleException(filterName, e)
        }
    }
    data object BusinessOwnerEmail: ExpenseFilterType() {
        override fun transform(value: String): () -> ColumnDeclaring<Boolean> {
            return { Businesses.ownerEmail eq value }
        }

    }
    data object MaxAmount: ExpenseFilterType() {
        override fun transform(value: String): () -> ColumnDeclaring<Boolean> {
            val amount = convertWithExceptionHandling(typeName) { value.toInt() }
            return { Expenses.amount lessEq amount }
        }
    }
    data object MinAmount: ExpenseFilterType() {
        override fun transform(value: String): () -> ColumnDeclaring<Boolean> {
            val amount = convertWithExceptionHandling(typeName) { value.toInt() }
            return { Expenses.amount greaterEq amount }
        }
    }
    data object Date: ExpenseFilterType() {
        override fun transform(value: String): () -> ColumnDeclaring<Boolean> {
            val date = convertWithExceptionHandling(typeName) { LocalDate.parse(value) }
            return { Expenses.date eq date }
        }
    }
    data object EarlierThan: ExpenseFilterType() {
        override fun transform(value: String): () -> ColumnDeclaring<Boolean> {
            val date = convertWithExceptionHandling(typeName) { LocalDate.parse(value) }
            return { Expenses.date lessEq date }
        }
    }
    data object LaterThan: ExpenseFilterType() {
        override fun transform(value: String): () -> ColumnDeclaring<Boolean> {
            val date = convertWithExceptionHandling(typeName) { LocalDate.parse(value) }
            return { Expenses.date greaterEq date }
        }
    }

    data object Month: ExpenseFilterType() {
        override fun transform(value: String): () -> ColumnDeclaring<Boolean> {
            return {
                val date = convertWithExceptionHandling(typeName) { LocalDate.parse(value) }
                val start = date.withDayOfMonth(1)
                val end = date.withDayOfMonth(date.lengthOfMonth())
                (Expenses.date greaterEq start) and (Expenses.date lessEq end)
            }
        }
    }
}
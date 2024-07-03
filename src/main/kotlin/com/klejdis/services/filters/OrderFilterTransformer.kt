package com.klejdis.services.filters

import com.klejdis.services.model.Orders
import org.ktorm.dsl.eq
import org.ktorm.dsl.greaterEq
import org.ktorm.dsl.lessEq
import org.ktorm.schema.ColumnDeclaring
import java.time.LocalDate


object OrderFilterTransformer: KtormFilterTransformer(OrderFilterType::class)

sealed class OrderFilterType: KtormFilterType() {
    data object MaxTotal: OrderFilterType() {
        override fun transform(value: String): () -> ColumnDeclaring<Boolean> {
            return { Orders.total lessEq value.toInt() }
        }
    }
    data object MinTotal: OrderFilterType() {
        override fun transform(value: String): () -> ColumnDeclaring<Boolean> {
            return { Orders.total greaterEq value.toInt() }
        }
    }
    data object Date: OrderFilterType() {
        override fun transform(value: String): () -> ColumnDeclaring<Boolean> {
            return { Orders.date eq LocalDate.parse(value) }
        }
    }
}
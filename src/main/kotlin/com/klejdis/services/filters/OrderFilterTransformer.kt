package com.klejdis.services.filters

import com.klejdis.services.model.Orders
import org.ktorm.dsl.eq
import org.ktorm.dsl.greaterEq
import org.ktorm.dsl.lessEq
import java.time.LocalDate


object OrderFilterTransformer: KtormFilterTransformer(OrderFilterCategory::class)

sealed class OrderFilterCategory: KtormFilterCategory() {
    data object MaxTotal: OrderFilterCategory() {
        override fun transform(value: String): KtormFilter {
            return KtormFilter(
                { Orders.total lessEq value.toInt() },
            )
        }
    }
    data object MinTotal: OrderFilterCategory() {
        override fun transform(value: String): KtormFilter {
            return KtormFilter(
                { Orders.total greaterEq value.toInt() },
            )
        }
    }
    data object Date: OrderFilterCategory() {
        override fun transform(value: String): KtormFilter {
            return KtormFilter(
                { Orders.date eq LocalDate.parse(value) },
            )
        }
    }

    data object MinDate: OrderFilterCategory() {
        override fun transform(value: String): KtormFilter {
            return KtormFilter(
                { Orders.date greaterEq LocalDate.parse(value) },
            )
        }
    }

    data object MaxDate: OrderFilterCategory() {
        override fun transform(value: String): KtormFilter {
            return KtormFilter(
                { Orders.date lessEq LocalDate.parse(value) },
            )
        }
    }
}
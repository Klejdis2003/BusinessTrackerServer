package com.klejdis.services.filters

import com.klejdis.services.model.Orders
import org.ktorm.dsl.eq
import org.ktorm.dsl.greaterEq
import org.ktorm.dsl.lessEq
import org.ktorm.schema.ColumnDeclaring
import java.time.LocalDate

/**
 * [kotlin.reflect.KClass.properties]
 */
class OrderFilterTransformer: KtormFilterTransformer(Type::class) {
    sealed class Type: KtormFilterType() {
        data object MaxTotal: Type() {
            override fun transform(value: String): () -> ColumnDeclaring<Boolean> {
                return { Orders.total lessEq value.toInt() }
            }
        }
        data object MinTotal: Type() {
            override fun transform(value: String): () -> ColumnDeclaring<Boolean> {
                return { Orders.total greaterEq value.toInt() }
            }
        }
        data object Date: Type() {
            override fun transform(value: String): () -> ColumnDeclaring<Boolean> {
                return { Orders.date eq LocalDate.parse(value) }
            }
        }
    }
}
package com.klejdis.services.filters

import com.klejdis.services.model.Items
import org.ktorm.dsl.greaterEq
import org.ktorm.dsl.lessEq
import org.ktorm.schema.ColumnDeclaring

class OrderFilterTransformer: KtormFilterTransformer(Type::class) {
    sealed class Type(name: String) : FilterType<() -> ColumnDeclaring<Boolean>>(name) {
        data object MaxItemPrice: Type("maxItemPrice") {
            override fun transform(value: String): () -> ColumnDeclaring<Boolean> {
                val maxItemPrice = value.toInt()
                return { Items.price lessEq maxItemPrice }
            }
        }
        data object MinItemPrice: Type("minItemPrice") {
            override fun transform(value: String): () -> ColumnDeclaring<Boolean> {
                val minItemPrice = value.toInt()
                return { Items.price greaterEq  minItemPrice }
            }
        }
    }
}
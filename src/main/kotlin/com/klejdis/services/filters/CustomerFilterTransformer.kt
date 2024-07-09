package com.klejdis.services.filters

import com.klejdis.services.model.Customers
import org.ktorm.dsl.like
import org.ktorm.dsl.or
import org.ktorm.schema.ColumnDeclaring

object CustomerFilterTransformer: KtormSimpleFilterTransformer(CustomerFilterCategory::class)

sealed class CustomerFilterCategory: KtormSimpleFilterCategory() {
    data object NameStartsWith: CustomerFilterCategory() {
        override fun transform(value: String):  () -> ColumnDeclaring<Boolean> {
            return { Customers.name like "$value%" }
        }
    }
    data object NameContains: CustomerFilterCategory() {
        override fun transform(value: String):  () -> ColumnDeclaring<Boolean> {
            return { Customers.name like "%$value%" }
        }
    }

    data object PhoneStartsWith: CustomerFilterCategory() {
        override fun transform(value: String):  () -> ColumnDeclaring<Boolean> {
            return { Customers.phone like "$value%" }
        }
    }

    data object PhoneContains: CustomerFilterCategory() {
        override fun transform(value: String):  () -> ColumnDeclaring<Boolean> {
            return { Customers.phone like "%$value%" }
        }
    }

    data object Contains: CustomerFilterCategory() {
        override fun transform(value: String): () -> ColumnDeclaring<Boolean> {
            return {
                Customers.name like "%$value%" or (Customers.phone like "%$value%")
            }
        }
    }
}

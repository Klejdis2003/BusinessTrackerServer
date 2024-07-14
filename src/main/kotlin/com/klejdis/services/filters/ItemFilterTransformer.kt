package com.klejdis.services.filters

import com.klejdis.services.model.Items
import org.ktorm.dsl.eq
import org.ktorm.dsl.like
import org.ktorm.schema.ColumnDeclaring

object ItemFilterTransformer : KtormSimpleFilterTransformer(ItemFilterCategory::class)

sealed class ItemFilterCategory : KtormSimpleFilterCategory(){
    data object Name : ItemFilterCategory() {
        override fun transform(value: String): () -> ColumnDeclaring<Boolean> {
            return { Items.name eq "%$value%" }
        }
    }

    data object Query : ItemFilterCategory() {
        override fun transform(value: String): () -> ColumnDeclaring<Boolean> {
            return { Items.name like "%$value%"}
        }
    }
    data object PurchasePrice : ItemFilterCategory() {
        override fun transform(value: String): () -> ColumnDeclaring<Boolean> {
            return { Items.purchasePrice eq value.toInt() }
        }
    }

    data object Price : ItemFilterCategory() {
        override fun transform(value: String): () -> ColumnDeclaring<Boolean> {
            return { Items.price eq value.toInt() }
        }
    }

    data object MinPrice : ItemFilterCategory() {
        override fun transform(value: String): () -> ColumnDeclaring<Boolean> {
            return { Items.price eq value.toInt() }
        }
    }

    data object MaxPrice : ItemFilterCategory() {
        override fun transform(value: String): () -> ColumnDeclaring<Boolean> {
            return { Items.price eq value.toInt() }
        }
    }


}
package com.klejdis.services.filters

import org.ktorm.schema.ColumnDeclaring
import kotlin.reflect.KClass

/**
 * Can transform a map of filters into a list of items of the desired type.
 * @param R type to be transformed into
 * @property filterTypeClass the sealed class that extends FilterType and contains the available filters
 */
abstract class FilterTransformer<out R>(private val filterTypeClass: KClass<out FilterType<R>>){
    /**
     * Uses reflection to get all the available filters from the sealed class.
     */
    private val availableFilters: Map<String, FilterType<R>> =
        filterTypeClass.sealedSubclasses.associate { it.objectInstance!!.name to it.objectInstance!! }

    /**
     * Transforms a map of filters into a list of filters that can be applied to the persistence layer.
     * @param values a map of filters where the key is the name of the filter and the value is the value of the filter
     * @return a list of filters that can be applied to the persistence layer
     * @throws NoSuchElementException if a filter is not found
     */
    fun generateTransformedFilters(values: Map<String, String>): List<R> {
        return values.map { (key, value) ->
            availableFilters[key]?.transform(value)
                ?: throw NoSuchElementException("Filter $key is invalid")
        }
    }
}

/**
 * A container for objects that are of this type. Override the transform function to transform a string value
 * for a filter into a filter that can be applied to the persistence layer.
 * @param R the type of filter that can be applied to the persistence layer
 * @property name the name of the filter, which needs to match the key in the map of filters
 */
sealed class FilterType<out R>(val name : String){
    abstract fun transform(value : String) : R
}

typealias KtormFilterTransformer = FilterTransformer<() -> ColumnDeclaring<Boolean>>
typealias KtormFilterType = FilterType<() -> ColumnDeclaring<Boolean>>
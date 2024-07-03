package com.klejdis.services.filters

import org.ktorm.schema.ColumnDeclaring
import kotlin.reflect.KClass

typealias Filter = Pair<String, String>
typealias TypeSafeFilter<T> = Pair<FilterType<T>, String>
typealias KtormFilterTransformer = FilterTransformer<() -> ColumnDeclaring<Boolean>>
typealias KtormFilterType = FilterType<() -> ColumnDeclaring<Boolean>>

/**
 * Can transform a map of filters into a list of items of the desired type. Uses reflection to get all the available
 * filters from the passed sealed class. All filter types under FilterType extension must be objects or data
 * objects that extend FilterType.
 * @param R type to be transformed into
 * @property filterTypeClass the sealed class that extends FilterType and contains the available filters
 */
abstract class FilterTransformer<R>(private val filterTypeClass: KClass<out FilterType<R>>){

    /**
     * Uses reflection to get all the available filters from the sealed class.
     */
    private val availableFilters: Map<String, FilterType<R>> =
        filterTypeClass.sealedSubclasses
            .associate {
                it.simpleName!!.replaceFirstChar{
                    char -> char.lowercase() } to it.objectInstance!!
            }
    private val errorMessage = "Filter \"%s\" is invalid. Please use one of the following filters: ${availableFilters.keys}."
    /**
     * Transforms a map of filters into a list of filters that can be applied to the persistence layer.
     * @param values a map of filters where the key is the name of the filter and the value is the value of the filter
     * @return a list of filters that can be applied to the persistence layer
     * @throws NoSuchElementException if a filter is not found
     */
    fun generateTransformedFilters(values: Map<String, String>): List<R> {
        return values.map { (key, value) ->
            availableFilters[key]?.transform(value)
                ?: throw NoSuchElementException(errorMessage.format(key))
        }
    }

    /**
     * Transforms a list of filters into a list of filters that can be applied to the persistence layer.
     * @param filters a list of [filters][Filter], which is just an alias for a [Pair] of 2 strings.
     * @return a list of filters that can be applied to the persistence layer
     * @throws NoSuchElementException if a filter is not found
     */
    fun generateTransformedFilters(filters: Iterable<Filter>): List<R> {
        return filters.map { (name, value) ->
            availableFilters[name]?.transform(value)
                ?: throw NoSuchElementException(errorMessage.format(name))
        }
    }

}


/**
 * A container for objects that are of this type. Override the transform function to transform a string value
 * for a filter into a filter that can be applied to the persistence layer.
 * @param R the type of filter that can be applied to the persistence layer
 */
sealed class FilterType<out R>{
    val typeName = this::class.simpleName!!.replaceFirstChar { char -> char.lowercase() }
    abstract fun transform(value : String) : R
}


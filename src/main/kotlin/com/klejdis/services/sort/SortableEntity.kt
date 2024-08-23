package com.klejdis.services.sort

import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties

/**
 * Class that represents an entity that can be sorted by its fields. Uses Kotlin Reflection
 * for all operations. The entity must inherit from this class to be able to use the sorting functionality through
 * the annotations.
 * It provides a way to get all the fields that can be sorted by, either by using [SortableField] annotations
 * or by using [EveryFieldSortable] annotation. The fields of type [SortableEntity] are also scanned for sortable fields recursively.
 * @see SortableField
 * @see EveryFieldSortable
 */
interface SortableEntity {

    private fun isFieldSortable(property: KProperty<*>, clazz: KClass<out SortableEntity>) =
        property.hasAnnotation<SortableField>() || (clazz.hasAnnotation<EveryFieldSortable>() && !clazz.findAnnotation<EveryFieldSortable>()!!.exclusions.contains(property.name))

    /**
     * Goes through all the fields of the entity and scans for either [SortableField] annotations
     * or [EveryFieldSortable]. If the latter is present, it will include all fields except the ones
     * specified in the exclusions array. Additionally, it also checks if there are fields that are
     * of type [SortableEntity] and recursively calls this function on them to get their fields.
     * Those do not need to be annotated with [SortableField] or [EveryFieldSortable]. The `nested`
     * fields will be prefixed with the name of the property that holds them.
     * Example Code:
     * ```
     * data class Order(
     *    @SortableField val id: Int,
     *    @SortableField val date: LocalDate,
     *    val customer: Customer
     * ): SortableEntity
     *
     * @EveryFieldSortable
     * data class Customer(
     *    val id: Int,
     *    val name: String
     * ): SortableEntity
     * ```
     * The `Order` entity will have the following sortable fields: `id`, `date`, `customer.id`, `customer.name`
     * because the `Customer` entity is annotated with [EveryFieldSortable] and the `Order.customer` property is of type `Customer`.
     *
     */
    fun getSortableFields(entity: SortableEntity = this, preface: String = ""): Set<String> {
        val clazz = entity::class
        val fields = HashSet<String>()


        clazz.memberProperties.forEach { property ->
            val propertyClass = property.returnType.classifier as? KClass<*>
            val value = property.call(entity)

            // Traverses the field tree using depth-first search if it encounters a child of `SortableEntity`
            if (propertyClass != null && propertyClass.isSubclassOf(SortableEntity::class)) { // Recursively get fields from nested entities if they are sortable
                fields.addAll(getSortableFields(value!! as SortableEntity, preface = "${property.name}."))
            }
            else {
                if (isFieldSortable(property, clazz)) fields.add("$preface${property.name}")
            }

        }
        return fields
    }


    fun isSortableField(fieldName: String) = getSortableFields().contains(fieldName)
    fun<T> isSortableProperty(property: KProperty<T>) = isSortableField(property.name)

    @Suppress("UNCHECKED_CAST")
    fun getPropertyValueByPath(propertyPath: String): Any? {
        val properties = propertyPath.split(".")
        var currentValue: Any? = this
        var currentClass: KClass<*> = this::class

        for (property in properties) {
            val prop = currentClass.memberProperties.find { it.name == property }
                ?: throw NoSuchFieldException("Property $property not found in ${currentClass.simpleName}")


            currentValue = (prop as? KProperty1<Any, *>)?.get(currentValue!!)
            currentClass = prop.returnType.classifier as KClass<*>
        }
        return currentValue
    }
}

enum class SortOrder {
    ASC, DESC;
    companion object {
        fun fromString(value: String?) = value.takeUnless { it.isNullOrBlank() }?.let { valueOf(it.uppercase()) } ?: ASC
    }
}


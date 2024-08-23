package com.klejdis.services.sort

import com.klejdis.services.model.EveryFieldSortable
import com.klejdis.services.model.SortOrder
import com.klejdis.services.model.SortableEntity
import com.klejdis.services.model.SortableField
import kotlinx.serialization.Serializable

/**
 * Utility class for sorting entities by a given field name and sort order.
 * @see SortableEntity
 * @see SortableField
 * @see SortOrder
 * @see
 */
object EntitySorter {

    /**
     * Sorts entities by a given field name and sort order. It is by no means type safe and the field name must
     * exactly match the name of the field in the entity class. It is meant to be used by the service layer, which
     * takes input from the client, which cannot pass type safe fields using Kotlin APIs.
     * @param entities list of entities to sort
     * @param fieldName name of the field to sort by, it must be annotated with [SortableField] and it
     * should exactly match the name of the field in the entity class.
     * @param sortOrder sort order
     * @return sorted list of entities
     * @throws IllegalArgumentException if the field is not sortable, i.e. it is not annotated with [SortableField]
     */
    @Suppress("UNCHECKED_CAST")
    inline fun<reified T: SortableEntity> sortEntities(entities: Iterable<T>, fieldName: String, sortOrder: SortOrder): List<T> {
        if(!entities.first().isSortableField(fieldName))
            throw IllegalArgumentException("Field $fieldName is not sortable. Sortable fields are: ${entities.first().getSortableFields()}")

        return when(sortOrder) {
            SortOrder.ASC -> entities.sortedBy { it.getPropertyValueByPath(fieldName) as Comparable<Any> }
            SortOrder.DESC -> entities.sortedByDescending { it.getPropertyValueByPath(fieldName) as Comparable<Any> }
            else -> entities.toList()
        }
    }
}

/**
 * Extension function for sorting entities by a given field name and sort order.
 * It does the same thing as [EntitySorter.sortEntities] but it is more convenient to use.
 * @param fieldName name of the field to sort by, it must be annotated with [SortableField] or the entire
 * class must be annotated with [EveryFieldSortable] and it should exactly match the name of the field in the entity class.
 * @param sortOrder sort order (ASC or DESC)
 * @return sorted list of entities
 * @throws IllegalArgumentException if the field is not sortable, i.e. it is not annotated with [SortableField]
 */
inline fun<reified T: SortableEntity> Iterable<T>.sortEntityByField(fieldName: String, sortOrder: SortOrder = SortOrder.ASC) = EntitySorter.sortEntities(this, fieldName, sortOrder)

@Serializable
class SortMethod private constructor(val fieldName: String?, val sortOrder: SortOrder?) {
    companion object {
        fun none(): SortMethod? = null
        fun of(fieldName: String?, sortOrder: SortOrder?): SortMethod? {
            if(fieldName.isNullOrBlank()) return null
            return SortMethod(fieldName, sortOrder)
        }
    }
}
package com.klejdis.services.sort

/**
 * Annotation for fields that can be sorted.
 * For the annotation to have any meaning, the entity must inherit from [SortableEntity].
 */
@Target(AnnotationTarget.PROPERTY)
annotation class SortableField

/**
 * Annotation for entities that have all fields sortable except the ones specified in the `exclusions` array.
 * For the annotation to have any meaning, the entity must inherit from [SortableEntity].
 */
annotation class EveryFieldSortable(val exclusions: Array<String> = [])

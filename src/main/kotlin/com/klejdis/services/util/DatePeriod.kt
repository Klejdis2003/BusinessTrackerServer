package com.klejdis.services.util

import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZonedDateTime

/**
 * Represents a time period that spans from a start date to an end date.
 * To make it easier to create TimePeriod objects, the class provides a few factory methods.
 * The factory methods are:
 * - [DatePeriod.max] Returns a TimePeriod that spans from the beginning of time to the end of time, essentially providing an unrestricted time period.
 * - [DatePeriod.from] Returns a TimePeriod that spans from the specified start date to the end of time.
 * - [DatePeriod.to] Returns a TimePeriod that spans from the beginning of time to the specified end date.
 * @property startDate The start date of the time period.
 * @property endDate The end date of the time period.
 * @constructor Creates a TimePeriod object.
 */
data class DatePeriod(
    val startDate: LocalDate,
    val endDate: LocalDate
) {
    init {
        require(startDate <= endDate) { "The start date must be before or equal to the end date." }
    }

    fun hasStartBound() = startDate != LocalDate.MIN
    fun hasEndBound() = endDate != LocalDate.MAX

    companion object {
        fun fromString(startDate: String?, endDate: String?): DatePeriod {
            return when {
                startDate == null && endDate == null -> max()
                startDate == null -> to(LocalDate.parse(endDate!!))
                endDate == null -> from(LocalDate.parse(startDate))
                else -> DatePeriod(
                    LocalDate.parse(startDate),
                    LocalDate.parse(endDate)
                )
            }

        }

        /**
         * @return A TimePeriod that spans from the beginning of time to the end of time.
         */
        fun max() = DatePeriod(
            LocalDate.MIN,
            LocalDate.MAX
        )

        /**
         * @param startDate The start date of the time period.
         * @return A TimePeriod that spans from the specified start date to the end of time.
         */
        fun from(startDate: LocalDate) = DatePeriod(
            startDate,
            LocalDate.MAX
        )

        /**
         * @param endDate The end date of the time period.
         * @return A TimePeriod that spans from the beginning of time to the specified end date.
         */
        fun to(startDate: LocalDate) = DatePeriod(
            LocalDate.MIN,
            startDate
        )
    }
}

fun getZonedDateTimeNow(): ZonedDateTime {
    return ZonedDateTime.now(ZoneOffset.UTC)
}


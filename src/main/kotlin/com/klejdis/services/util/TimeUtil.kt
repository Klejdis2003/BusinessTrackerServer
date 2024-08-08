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

class TimeFrame private constructor(
    val days: Long,
    val hours: Short,
    val minutes: Short,
    val seconds: Short
) {
    companion object {
        //constants
        const val SECONDS_IN_MINUTE = 60
        const val MINUTES_IN_HOUR = 60
        const val HOURS_IN_DAY = 24

        //derived constants
        const val SECONDS_IN_HOUR = SECONDS_IN_MINUTE * MINUTES_IN_HOUR
        const val SECONDS_IN_DAY = SECONDS_IN_HOUR * HOURS_IN_DAY
        const val MINUTES_IN_DAY = MINUTES_IN_HOUR * HOURS_IN_DAY

        /**
         * Creates a Time object from the specified number of seconds.
         * @param seconds The number of seconds to convert to a Time object.
         * @return A TimeFrame object that breaks down the specified number of seconds into days, hours, minutes, and seconds.
         */
        fun seconds(seconds: Long): TimeFrame {
            val remainingSeconds = LongWrapper(seconds)
            val days = remainingSeconds.divideAndApplyRemainder(SECONDS_IN_DAY)
            val hours = remainingSeconds.divideAndApplyRemainder(SECONDS_IN_HOUR)
            val minutes = remainingSeconds.divideAndApplyRemainder(SECONDS_IN_MINUTE)

            return TimeFrame(days, hours.toShort(), minutes.toShort(), remainingSeconds.value.toShort())
        }

        fun indefinite(): TimeFrame = TimeFrame(Long.MAX_VALUE, 0, 0, 0)

        /**
         * Creates a Time object from the specified number of minutes.
         * @param minutes The number of minutes to convert to a Time object.
         * @return A TimeFrame object that breaks down the specified number of minutes into days, hours, minutes.
         */
        fun minutes(minutes: Long): TimeFrame {
            val remainingMinutes = LongWrapper(minutes)
            val days = remainingMinutes.divideAndApplyRemainder(MINUTES_IN_DAY)
            val hours = remainingMinutes.divideAndApplyRemainder(MINUTES_IN_HOUR)

            return TimeFrame(days, hours.toShort(), remainingMinutes.value.toShort(), 0)
        }

        /**
         * Creates a Time object from the specified number of hours.
         * @param hours The number of hours to convert to a Time object.
         * @return A TimeFrame object that breaks down the specified number of hours into days, hours.
         */
        fun hours(hours: Long): TimeFrame {
            val remainingHours = LongWrapper(hours)
            val days = remainingHours.divideAndApplyRemainder(HOURS_IN_DAY)

            return TimeFrame(days, remainingHours.value.toShort(), 0, 0)
        }

        /**
         * Creates a Time object from the specified number of days.
         * @param days The number of days to convert to a Time object.
         * @return A TimeFrame object with the specified number of days.
         */
        fun days(days: Long): TimeFrame {
            return TimeFrame(days, 0, 0, 0)
        }
    }

    /**
     * @param other The other TimeFrame object
     * @return A TimeFrame object that represents the sum of this TimeFrame and the other TimeFrame.
     * The sum is calculated by adding the days, hours, minutes, and seconds of the two TimeFrame objects and
     * carrying over the excess values to the next higher unit of time.
     */
    operator fun plus(other: TimeFrame): TimeFrame {
        val hoursWrapper = LongWrapper(hours.plus(other.hours).toLong())
        val minutesWrapper = LongWrapper.fromInt(minutes + other.minutes)
        val secondsWrapper = LongWrapper.fromInt(seconds + other.seconds)

        val days = this.days + other.days + hoursWrapper.divideAndApplyRemainder(HOURS_IN_DAY)
        val hours = hoursWrapper.value + minutesWrapper.divideAndApplyRemainder(MINUTES_IN_HOUR)
        val minutes = minutesWrapper.value + secondsWrapper.divideAndApplyRemainder(SECONDS_IN_MINUTE)
        val seconds = secondsWrapper.value

        return TimeFrame(days, hours.toShort(), minutes.toShort(), seconds.toShort())
    }

    fun addSeconds(seconds: Long): TimeFrame = this + seconds(seconds)
    fun addMinutes(minutes: Long): TimeFrame = this + minutes(minutes)
    fun addHours(hours: Long): TimeFrame = this + hours(hours)
    fun addDays(days: Long): TimeFrame = this + days(days)



    fun toSeconds(): Long =
        days * SECONDS_IN_DAY + hours * SECONDS_IN_HOUR + minutes * SECONDS_IN_MINUTE + seconds

    fun toMinutes(): Long =
        days * MINUTES_IN_DAY + hours * MINUTES_IN_HOUR + minutes

    fun toHours(): Long =
        days * HOURS_IN_DAY + hours


        private class LongWrapper(var value: Long) {
            companion object {
                fun fromInt(value: Int) = LongWrapper(value.toLong())
            }
        operator fun div(other: Long): Long {
            val result = value / other
            value = value.rem(other)
            return result
        }

        fun divideAndApplyRemainder(other: Long): Long {
            val result = value / other
            value %= other
            return result
        }

        fun divideAndApplyRemainder(other: Int) = divideAndApplyRemainder(other.toLong())

    }
}


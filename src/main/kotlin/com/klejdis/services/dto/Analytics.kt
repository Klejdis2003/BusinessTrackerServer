package com.klejdis.services.dto

import com.klejdis.services.sort.EveryFieldSortable
import com.klejdis.services.sort.SortableEntity
import com.klejdis.services.util.DatePeriod
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class Analytics(
    val mostPopularItems: List<MostPopularItemDto>,
    val mostProfitableItems: List<MostProfitableItemDto>?,
    val mostProfitableCustomers: List<MostProfitableCustomerDto>?,
    val totalProfit: Int,
    val totalRevenue: Int,
    val totalExpenses: Int,
    val totalCustomers: Int,
    val soldItemsNumber: Int,
    val orderNumber: Int,
    val timePeriod: TimePeriodDto
)

@Serializable
data class TimePeriodDto(
    val startDate: String,
    val endDate: String
) {
    companion object {
        fun fromTimePeriod(datePeriod: DatePeriod) = TimePeriodDto(
            datePeriod.hasStartBound().takeIf { it }?.let { datePeriod.startDate.toString() } ?: "",
            datePeriod.hasEndBound().takeIf { it }?.let { datePeriod.endDate.toString() } ?: ""
        )
        fun toTimePeriod(timePeriodDto: TimePeriodDto) = DatePeriod(
            timePeriodDto.startDate.takeIf { it.isNotEmpty() }?.let { LocalDate.parse(it) } ?: LocalDate.MIN,
            timePeriodDto.endDate.takeIf { it.isNotEmpty() }?.let { LocalDate.parse(it) } ?: LocalDate.MAX
        )
    }
}

@Serializable
data class MostProfitableItemDto(
    val item: ItemDto,
    val profit: Int
)

@Serializable
data class MostProfitableCustomerDto(
    val customer: CustomerDto,
    val profit: Int
)

@Serializable
data class MostPopularItemDto(
    val item: ItemDto,
    val count: Int
)

@Serializable
@EveryFieldSortable(exclusions = ["item"])
data class ItemStat(
    val item: ItemDto,
    val sales: Int,
    val totalProfit: Int,
    val totalRevenue: Int,
    val totalExpenses: Int
) : SortableEntity
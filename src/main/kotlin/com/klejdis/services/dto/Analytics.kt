package com.klejdis.services.dto

import com.klejdis.services.util.DatePeriod
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class Analytics(
    val mostPopularItems: List<MostPopularItemDto>,
    val mostProfitableItems: List<MostProfitableItemDto>?,
    val mostProfitableCustomers: List<MostProfitableCustomerDto>?,
    val totalProfit: Int,
    val timePeriod: TimePeriodDto
)

@Serializable
data class TimePeriodDto(
    val startDate: String,
    val endDate: String
) {
    companion object {
        fun fromTimePeriod(datePeriod: DatePeriod) = TimePeriodDto(
            datePeriod.startDate.toString(),
            datePeriod.endDate.toString()
        )
        fun toTimePeriod(timePeriodDto: TimePeriodDto) = DatePeriod(
            LocalDate.parse(timePeriodDto.startDate),
            LocalDate.parse(timePeriodDto.endDate)
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

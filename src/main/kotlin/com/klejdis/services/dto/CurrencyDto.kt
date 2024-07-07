package com.klejdis.services.dto

import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class CurrencyDto(
    val code: String,
    val name: String,
    val symbol: String
) {
    companion object {
        fun fromCurrency(currency: Currency): CurrencyDto {
            return CurrencyDto(
                code = currency.currencyCode,
                name = currency.displayName,
                symbol = currency.symbol
            )
        }
    }

}

object CurrencyMapper {
    fun toDto(currency: Currency): CurrencyDto {
        return CurrencyDto(
            code = currency.currencyCode,
            name = currency.displayName,
            symbol = currency.symbol
        )
    }
}
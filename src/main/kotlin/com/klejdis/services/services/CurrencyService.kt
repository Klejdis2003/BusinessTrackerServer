package com.klejdis.services.services

import com.klejdis.services.dto.CurrencyDto
import java.util.*

class CurrencyService {
    fun getAll(query: String = "") =
        Currency
            .getAvailableCurrencies()
            .filter { it.displayName.contains(query, ignoreCase = true)  || it.currencyCode.contains(query, ignoreCase = true)}
            .map { CurrencyDto.fromCurrency(it) }

    fun getAllCodes(query: String = "") =
        Currency.getAvailableCurrencies()
            .filter { it.currencyCode.contains(query, ignoreCase = true) }
            .map { it.currencyCode }
}
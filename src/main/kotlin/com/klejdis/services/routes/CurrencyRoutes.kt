package com.klejdis.services.routes

import com.klejdis.services.extensions.respondWithExceptionHandling
import com.klejdis.services.services.CurrencyService
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.currencyRoutes() {
    val currencyService by inject<CurrencyService>()
    get("/currencies") {
        val query = call.request.queryParameters["query"] ?: ""
        call.respondWithExceptionHandling(currencyService.getAll(query))
    }
}
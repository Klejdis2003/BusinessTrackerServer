package com.klejdis.services.routes

import com.klejdis.services.dto.ExpenseMapper
import com.klejdis.services.filters.*
import com.klejdis.services.model.Expense
import com.klejdis.services.repositories.BusinessRepository
import com.klejdis.services.repositories.ExpenseRepository
import com.klejdis.services.services.ExpenseService
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import org.koin.ktor.ext.inject

fun Route.expenseRoutes() {
    val expenseService by inject<ExpenseService>()
    route("/expenses") {
        get {
            val queryParameters: List<Filter> = call.request.queryParameters.flattenEntries()
            call.getProfileInfoFromSession()?.email?.let {
                val expenses = expenseService.getAll(it, queryParameters)
                call.respond(HttpStatusCode.OK, expenses)
            }
        }
    }
}
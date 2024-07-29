package com.klejdis.services.routes

import com.klejdis.services.dto.ExpenseCreationDto
import com.klejdis.services.extensions.executeWithExceptionHandling
import com.klejdis.services.extensions.getScopedService
import com.klejdis.services.filters.Filter
import com.klejdis.services.services.ExpenseService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*

fun Route.expenseRoutes() {

    route("/expenses") {
        get {
            val expenseService = call.getScopedService<ExpenseService>()
            val queryParameters: List<Filter> = call.request.queryParameters.flattenEntries()
            call.executeWithExceptionHandling {
                val expenses = expenseService.getAll(queryParameters)
                call.respond(expenses)

            }
        }
        post {
            val expenseService = call.getScopedService<ExpenseService>()
            call.executeWithExceptionHandling {
                val expense = call.receive<ExpenseCreationDto>()
                val createdExpense = expenseService.create(expense)
                call.respond(HttpStatusCode.Created, createdExpense)
            }
        }
    }
}
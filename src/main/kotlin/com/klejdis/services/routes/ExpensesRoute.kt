package com.klejdis.services.routes

import com.klejdis.services.dto.ExpenseCreationDto
import com.klejdis.services.filters.Filter
import com.klejdis.services.plugins.executeWithExceptionHandling
import com.klejdis.services.services.ExpenseService
import io.ktor.http.*
import io.ktor.server.request.*
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
                call.executeWithExceptionHandling {
                    val expenses = expenseService.getAll(it, queryParameters)
                    call.respond(expenses)
                }
            }
        }
        post {
            call.getProfileInfoFromSession()?.email?.let { email ->
                call.executeWithExceptionHandling {
                    val expense = call.receive<ExpenseCreationDto>()
                    val createdExpense = expenseService.create(expense, email)
                    call.respond(HttpStatusCode.Created, createdExpense)
                }
            }
        }
    }
}
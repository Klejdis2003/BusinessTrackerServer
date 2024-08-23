package com.klejdis.services.routes


import com.klejdis.services.extensions.executeWithExceptionHandling
import com.klejdis.services.extensions.getScopedService
import com.klejdis.services.model.SortOrder
import com.klejdis.services.services.AnalyticsService
import com.klejdis.services.sort.SortMethod
import com.klejdis.services.util.DatePeriod
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.analyticsRoute() {
    /**
     * Executes the block with the analytics context. The context includes the analytics service and the date period.
     */
    suspend fun ApplicationCall.withAnalyticsContext(block: suspend (AnalyticsService, DatePeriod) -> Unit) {
        val analyticsService = getScopedService<AnalyticsService>()
        val startDate = parameters["startDate"]
        val endDate = parameters["endDate"]
        val datePeriod = DatePeriod.fromString(startDate, endDate)
        executeWithExceptionHandling {
            block(analyticsService, datePeriod)
        }
    }
    route("/analytics") {
        get {
            call.withAnalyticsContext { analyticsService, datePeriod ->
                call.respond(analyticsService.getAnalytics(datePeriod))
            }
        }

        get("/topCustomers") {
            call.withAnalyticsContext { analyticsService, datePeriod ->
                val limit = call.parameters["limit"]?.toIntOrNull()
                call.respond(
                    limit?.let { analyticsService.getTopCustomers(datePeriod, it) }
                        ?: analyticsService.getTopCustomers(datePeriod)
                )
            }
        }

        get("/items") {
            call.withAnalyticsContext { analyticsService, datePeriod ->
                val limit = call.parameters["limit"]?.toIntOrNull()
                val sortBy = call.parameters["sortBy"]
                val sortOrder = call.parameters["order"]
                val sortMethod = SortMethod.of(sortBy, SortOrder.fromString(sortOrder))
                call.respond(analyticsService.getItemStats(datePeriod, sortMethod))
            }
        }
    }

}
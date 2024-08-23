package com.klejdis.services.routes


import com.klejdis.services.extensions.executeWithExceptionHandling
import com.klejdis.services.extensions.getScopedService
import com.klejdis.services.services.AnalyticsService
import com.klejdis.services.sort.SortMethod
import com.klejdis.services.sort.SortOrder
import com.klejdis.services.util.DatePeriod
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.analyticsRoute() {
    /**
     * Provides context for the analytics route with the common parameters for all endpoints.
     * It reduces boilerplate code and provides a way to handle exceptions in a single place.
     * @param block the block of code that will be executed with the analytics context
     */
    suspend fun ApplicationCall.withAnalyticsContext(block: suspend ApplicationCall.(AnalyticsService, DatePeriod) -> Unit) {
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
                respond(analyticsService.getAnalytics(datePeriod))
            }
        }

        get("/topCustomers") {
            call.withAnalyticsContext { analyticsService, datePeriod ->
                val limit = call.parameters["limit"]?.toIntOrNull()
                respond(
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
                respond(analyticsService.getItemStats(datePeriod, sortMethod))
            }
        }
    }

}
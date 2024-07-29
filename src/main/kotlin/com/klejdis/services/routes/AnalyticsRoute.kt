package com.klejdis.services.routes


import com.klejdis.services.extensions.executeWithExceptionHandling
import com.klejdis.services.extensions.getScopedService
import com.klejdis.services.extensions.respondWithExceptionHandling
import com.klejdis.services.services.AnalyticsService
import com.klejdis.services.util.DatePeriod
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.analyticsRoute() {
    route("/analytics") {
        get {
            val analyticsService = call.getScopedService<AnalyticsService>()
            val startDate = call.parameters["startDate"]
            val endDate = call.parameters["endDate"]
            val datePeriod = DatePeriod.fromString(startDate, endDate)

            call.executeWithExceptionHandling {
                it.respond(analyticsService.getAnalytics(datePeriod))
            }
        }

        get("/topCustomers") {
            val analyticsService = call.getScopedService<AnalyticsService>()
            val startDate = call.parameters["startDate"]
            val endDate = call.parameters["endDate"]
            val limit = call.parameters["limit"]?.toIntOrNull()
            val datePeriod = DatePeriod.fromString(startDate, endDate)

            call.respondWithExceptionHandling(
                limit?.let { analyticsService.getTopCustomers(datePeriod, it) }
                    ?: analyticsService.getTopCustomers(datePeriod)
            )
        }
    }

}
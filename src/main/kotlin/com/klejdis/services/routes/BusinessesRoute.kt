package com.klejdis.services.routes

import com.klejdis.services.plugins.executeWithExceptionHandling
import com.klejdis.services.services.BusinessService
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.businessesRoute() {

    get("/customers") {
        val businessService = call.getScopedService<BusinessService>()
        val customers = businessService.getCustomers()
        call.executeWithExceptionHandling {
            call.respond(customers)
        }
    }
}


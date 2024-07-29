package com.klejdis.services.routes

import com.klejdis.services.extensions.getScopedService
import com.klejdis.services.extensions.respondWithExceptionHandling
import com.klejdis.services.services.BusinessService
import io.ktor.server.routing.*

fun Route.businessesRoute() {

    get("/customers") {
        val businessService = call.getScopedService<BusinessService>()
        val customers = businessService.getCustomers()
        call.respondWithExceptionHandling(customers)
    }
}


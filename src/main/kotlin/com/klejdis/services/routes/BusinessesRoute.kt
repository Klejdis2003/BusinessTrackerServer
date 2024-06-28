package com.klejdis.services.routes

import com.klejdis.services.services.BusinessService
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.businessesRoute() {
    val businessService by inject<BusinessService>()
    route("/businesses") {
        get {
            val ownerEmail = call.getProfileInfoFromSession()?.email
            if (ownerEmail == null) call.respond(HttpStatusCode.BadRequest, "Invalid token")

            val business = businessService.getByEmail(ownerEmail!!)

            if (business != null) call.respond(HttpStatusCode.OK, business)
            else
                call.respond(HttpStatusCode.NotFound, "Business with email $ownerEmail not found")
        }
    }
}


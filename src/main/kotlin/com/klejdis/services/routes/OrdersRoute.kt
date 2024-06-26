package com.klejdis.services.routes

import com.klejdis.services.services.OrderService
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.ordersRoute() {
    val orderService: OrderService by inject()
    route("/orders") {
        get {
            val business = call.getProfileInfoFromSession() ?: return@get call.respond(HttpStatusCode.Unauthorized)
            val orders = orderService.getByBusinessOwnerEmail(business.email)
            call.respond(orders)
        }
        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            val business = call.getProfileInfoFromSession() ?: return@get call.respond(HttpStatusCode.Unauthorized)
            val order = id?.let { orderService.getByIdAndBusinessOwnerEmail(id, business.email) }
            order?.let { call.respond(order) }
                ?: call.respond(
                    HttpStatusCode.NotFound,
                    "Your business does not have an order with id=$id"
                )
        }
    }
}
package com.klejdis.services.routes

import com.klejdis.services.dto.OrderCreationDto
import com.klejdis.services.plugins.executeWithExceptionHandling
import com.klejdis.services.services.OrderService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import org.koin.ktor.ext.inject

fun Route.ordersRoute() {
    val orderService: OrderService by inject()
    route("/orders") {
        get {
            val business = call.getProfileInfoFromSession() ?: return@get call.respond(HttpStatusCode.Unauthorized)
            val filters = call.request.queryParameters.flattenEntries()
            call.executeWithExceptionHandling {
                val orders = orderService.getByBusinessOwnerEmail(business.email, filters)
                call.respond(orders)
            }
        }
        post {
            val business = call.getProfileInfoFromSession() ?: return@post call.respond(HttpStatusCode.Unauthorized)
            call.executeWithExceptionHandling {
                val order = call.receive<OrderCreationDto>()
                val newOrder = orderService.create(order, business.email)
                call.respond(newOrder)
            }
        }
        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            val business = call.getProfileInfoFromSession() ?: return@get call.respond(HttpStatusCode.Unauthorized)
            val order = id?.let { orderService.get(id, business.email) }
            order?.let { call.respond(order) }
                ?: call.respond(
                    HttpStatusCode.NotFound,
                    "Business with email=${business.email} does not have an order with id=$id"
                )
        }
        get("/top") {
            val business = call.getProfileInfoFromSession() ?: return@get call.respond(HttpStatusCode.Unauthorized)
            call.executeWithExceptionHandling {
                val order = orderService.getMostExpensiveOrder(business.email)
                order?.let { call.respond(order) }
                    ?: call.respond(HttpStatusCode.NotFound, "Your business does not have any orders")
            }
        }
    }
}
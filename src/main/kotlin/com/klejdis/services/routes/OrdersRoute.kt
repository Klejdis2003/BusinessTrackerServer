package com.klejdis.services.routes

import com.klejdis.services.dto.OrderCreationDto
import com.klejdis.services.plugins.executeWithExceptionHandling
import com.klejdis.services.services.OrderService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*

fun Route.ordersRoute() {
    route("/orders") {
        get {
            val orderService =  call.getScopedService<OrderService>()
            val filters = call.request.queryParameters.flattenEntries()
            call.executeWithExceptionHandling {
                val orders = orderService.getAllBusinessOrders(filters)
                call.respond(orders)
            }
        }
        post {
            val orderService =  call.getScopedService<OrderService>()
            call.executeWithExceptionHandling {
                val order = call.receive<OrderCreationDto>()
                val newOrder = orderService.create(order)
                call.respond(newOrder)
            }
        }
        get("/{id}") {
            val orderService =  call.getScopedService<OrderService>()
            val id = call.parameters["id"]?.toIntOrNull()
            val business = call.getProfileInfoFromSession() ?: return@get call.respond(HttpStatusCode.Unauthorized)
            val order = id?.let { orderService.get(id) }
            order?.let { call.respond(order) }
                ?: call.respond(
                    HttpStatusCode.NotFound,
                    "Business with email=${business.email} does not have an order with id=$id"
                )
        }
        get("/top") {
            val orderService =  call.getScopedService<OrderService>()
            call.executeWithExceptionHandling {
                val order = orderService.getMostExpensiveOrder()
                order?.let { call.respond(order) }
                    ?: call.respond(HttpStatusCode.NotFound, "Your business does not have any orders")
            }
        }
    }
}
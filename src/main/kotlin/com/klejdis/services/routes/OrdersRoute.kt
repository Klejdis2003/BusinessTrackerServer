package com.klejdis.services.routes

import com.klejdis.services.dto.OrderCreationDto
import com.klejdis.services.extensions.executeWithExceptionHandling
import com.klejdis.services.extensions.getScopedService
import com.klejdis.services.services.OrderService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*

fun Route.ordersRoute() {
    route("/orders") {
        get {
            call.executeWithExceptionHandling {
                val orderService =  it.getScopedService<OrderService>()
                val filters = call.request.queryParameters.flattenEntries()
                val orders = orderService.getAllBusinessOrders(filters)
                it.respond(orders)
            }
        }
        post {
            call.executeWithExceptionHandling {
                val orderService = it.getScopedService<OrderService>()
                val order = call.receive<OrderCreationDto>()
                val newOrder = orderService.create(order)
                it.respond(newOrder)
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
            call.executeWithExceptionHandling {
                val orderService = it.getScopedService<OrderService>()
                val order = orderService.getMostExpensiveOrder()
                order?.let { o -> call.respond(o) }
                    ?: call.respond(HttpStatusCode.NotFound, "Your business does not have any orders")
            }
        }
    }
}
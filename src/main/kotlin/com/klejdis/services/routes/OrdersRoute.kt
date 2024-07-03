package com.klejdis.services.routes

import com.klejdis.services.dto.OrderCreationDto
import com.klejdis.services.plugins.handleException
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
            try{
                val orders = orderService.getByBusinessOwnerEmail(business.email, filters)
                call.respond(orders)
            } catch (e: Exception) {
                call.handleException(e)
            }

        }
        post {
            val business = call.getProfileInfoFromSession() ?: return@post call.respond(HttpStatusCode.Unauthorized)
            try {
                val order = call.receive<OrderCreationDto>()
                val newOrder = orderService.create(order, business.email)
                call.respond(newOrder)
            } catch (e: Exception) {
                call.handleException(e)
            }
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
        get("/top") {
            val business = call.getProfileInfoFromSession() ?: return@get call.respond(HttpStatusCode.Unauthorized)
            try {
                val order = orderService.getMostExpensiveOrder(business.email)
                order?.let { call.respond(order) }
                    ?: call.respond(HttpStatusCode.NotFound, "Your business does not have any orders")
            } catch (e: Exception) {
                call.handleException(e)
            }
        }
    }
}
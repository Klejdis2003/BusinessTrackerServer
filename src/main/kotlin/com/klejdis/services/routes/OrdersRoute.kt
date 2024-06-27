package com.klejdis.services.routes

import com.klejdis.services.dto.OrderCreationDto
import com.klejdis.services.services.EntityAlreadyExistsException
import com.klejdis.services.services.EntityNotFoundException
import com.klejdis.services.services.OrderService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.ordersRoute() {
    val orderService: OrderService by inject()

    val handleException: suspend (Exception, RoutingCall) -> Unit = { e, call ->
        val httpStatusCode = when (e) {
            is EntityNotFoundException -> HttpStatusCode.NotFound
            is EntityAlreadyExistsException -> HttpStatusCode.Conflict
            is IllegalArgumentException -> HttpStatusCode.BadRequest
            else -> HttpStatusCode.InternalServerError
        }
        call.respond(httpStatusCode, e.message ?: "Unknown error")
    }

    route("/orders") {
        get {
            val business = call.getProfileInfoFromSession() ?: return@get call.respond(HttpStatusCode.Unauthorized)
            val orders = orderService.getByBusinessOwnerEmail(business.email)
            call.respond(orders)
        }
        post {
            val business = call.getProfileInfoFromSession() ?: return@post call.respond(HttpStatusCode.Unauthorized)
            val order = call.receive<OrderCreationDto>()
            try {
                val newOrder = orderService.create(order, business.email)
                call.respond(newOrder)
            } catch (e: Exception) {
                handleException(e, call)
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
    }
}
package com.klejdis.services.routes

import com.klejdis.services.dto.OrderCreationDto
import com.klejdis.services.services.EntityAlreadyExistsException
import com.klejdis.services.services.EntityNotFoundException
import com.klejdis.services.services.OrderService
import com.klejdis.services.services.printStackTraceIfInDevMode
import io.ktor.http.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import org.koin.ktor.ext.inject

fun Route.ordersRoute() {
    val orderService: OrderService by inject()

    val handleException: suspend (Exception, RoutingCall) -> Unit = { e, call ->
        e.printStackTraceIfInDevMode()
        when (e) {
            is EntityNotFoundException -> call.respond(HttpStatusCode.NotFound, e.message!!)
            is EntityAlreadyExistsException -> call.respond(HttpStatusCode.Conflict, e.message!!)
            is IllegalArgumentException -> call.respond(HttpStatusCode.BadRequest, e.message!!)
            is BadRequestException -> {
                val message = e.cause?.message?.substringBefore("for") ?: e.message
                call.respond(HttpStatusCode.BadRequest, message ?: "Missing required fields.")
            }

            else -> call.respond(HttpStatusCode.InternalServerError, "An unexpected error occurred.")
        }

    }

    route("/orders") {
        get {
            val business = call.getProfileInfoFromSession() ?: return@get call.respond(HttpStatusCode.Unauthorized)
            val filters = call.request.queryParameters.toMap().mapValues {it.value.first()}
            val orders = orderService.getByBusinessOwnerEmail(business.email, filters)
            call.respond(orders)
        }
        post {
            val business = call.getProfileInfoFromSession() ?: return@post call.respond(HttpStatusCode.Unauthorized)
            try {
                val order = call.receive<OrderCreationDto>()
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
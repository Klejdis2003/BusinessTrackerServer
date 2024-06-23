package com.klejdis.services.routes

import com.klejdis.services.dto.OrderDto
import com.klejdis.services.dto.OrderMapper
import com.klejdis.services.plugins.getSession
import com.klejdis.services.repositories.OrderRepository
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import kotlin.text.get

fun Route.ordersRoute() {
    val orderRepository: OrderRepository by inject()
    val orderMapper: OrderMapper by inject()
    route("/orders") {
        get {
            call.respondText("Orders route")
        }
        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            val order = id?.let { orderRepository.get(it) }
            println(order)
            val business = call.getProfileInfoFromSession()
            if(business?.email != order?.business?.ownerEmail){
                call.respond(HttpStatusCode.Forbidden, "You are not allowed to access this resource")
            }
            else {
                order?.let {
                    call.respond(orderMapper.toOrderDto(it))
                } ?: call.respond(HttpStatusCode.NotFound, "Order not found")
            }
        }
    }
}
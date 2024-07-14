package com.klejdis.services.routes

import com.klejdis.services.dto.ItemCreationDto
import com.klejdis.services.extensions.executeWithExceptionHandling
import com.klejdis.services.services.ItemService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*

fun Route.itemsRoute(){
    route("/items"){
        get{
            val itemService = call.getScopedService<ItemService>()

            val filters = call.request.queryParameters.flattenEntries()
            call.executeWithExceptionHandling {
                it.respond(itemService.getAll(filters))
            }
        }

        post {
            val itemService = call.getScopedService<ItemService>()
            call.executeWithExceptionHandling {
                val item = call.receive<ItemCreationDto>()
                val newItem = itemService.create(item)
                it.respond(HttpStatusCode.Created, newItem)
            }
        }
    }
}
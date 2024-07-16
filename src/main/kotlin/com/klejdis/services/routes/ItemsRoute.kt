package com.klejdis.services.routes

import com.klejdis.services.extensions.executeWithExceptionHandling
import com.klejdis.services.services.ItemService
import com.klejdis.services.util.FileOperations
import io.ktor.http.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import java.io.File

const val ITEM_IMAGES_ENDPOINT = "items/image"

fun Route.itemsRoute(){
    route("/items"){
        staticFiles("/images", File("${FileOperations.IMAGE_DIR}/item")) {
            this.modify{ resource, call ->
                if(resource.name == "default.jpg") return@modify
                val itemService = call.getScopedService<ItemService>()
                itemService.get(resource.name) ?: call.respond(HttpStatusCode.NotFound, "Item not found")
            }
        }

        get{
            val itemService = call.getScopedService<ItemService>()

            val filters = call.request.queryParameters.flattenEntries()
            call.executeWithExceptionHandling {
                it.respond(itemService.getAll(filters))
            }
        }
        get("/{id}"){
            val itemService = call.getScopedService<ItemService>()
            val item = itemService.get(call.parameters["id"]!!.toInt()) ?: call.respond(HttpStatusCode.NotFound, "Item not found")
            call.respond(item)
        }

        post {
            val itemService = call.getScopedService<ItemService>()
            call.executeWithExceptionHandling {
                val multiPartData = call.receiveMultipart()
                val newItem = itemService.create(multiPartData)
                call.respond(HttpStatusCode.Created, newItem)
            }
        }
        put("/{id}") {
            call.executeWithExceptionHandling {
                val itemService = it.getScopedService<ItemService>()
                val id = it.parameters["id"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid id")
                val multiPartData = it.receiveMultipart()
                val updatedItem = itemService.updateImage(id, multiPartData)
                it.respond(HttpStatusCode.OK, updatedItem)
            }
        }

        delete("/{id}") {
            val itemService = call.getScopedService<ItemService>()
            call.executeWithExceptionHandling {
                val id = call.parameters["id"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid id")
                itemService.delete(id)
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}
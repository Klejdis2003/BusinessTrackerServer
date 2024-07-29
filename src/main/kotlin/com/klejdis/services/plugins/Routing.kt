package com.klejdis.services.plugins

import com.klejdis.services.routes.*
import com.klejdis.services.util.FileOperations
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*
import java.io.File

const val HOME_ROUTE = "/orders"
const val DEFAULT_IMAGES_ENDPOINT = "/images"

fun Application.configureRouting() {
    routing {
        authRoute()
        businessesRoute()
        expenseRoutes()
        ordersRoute()
        analyticsRoute()
        itemsRoute()
        currencyRoutes()
        staticFiles(remotePath = "/code_documentation", File("src/main/resources/documentation/code"))
        staticFiles(remotePath = DEFAULT_IMAGES_ENDPOINT, File("uploads/images"))
        openAPI(path = "openapi", swaggerFile = FileOperations.getResourceAsRelativePath("openapi/documentation.yaml"))
        swaggerUI(path="swagger", swaggerFile = FileOperations.getResourceAsRelativePath("openapi/documentation.yaml"))
    }
}







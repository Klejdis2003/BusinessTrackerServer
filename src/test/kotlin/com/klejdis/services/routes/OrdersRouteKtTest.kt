package com.klejdis.services.routes

import com.klejdis.services.plugins.configureRouting
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test

class OrdersRouteKtTest {

    @Test
    fun testGetOrders() = testApplication {
        application {
            configureRouting()
        }
        client.get("/orders").apply {
            assert(this.status == HttpStatusCode.Unauthorized)
        }
    }
}
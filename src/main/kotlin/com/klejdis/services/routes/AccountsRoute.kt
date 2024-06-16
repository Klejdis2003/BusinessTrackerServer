package com.klejdis.services.routes

import com.klejdis.services.model.Account
import com.klejdis.services.plugins.AuthMethod
import com.klejdis.services.services.AccountService
import com.klejdis.services.services.EntityAlreadyExistsException
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.accountsRoute() {
    val accountService by inject<AccountService>()
    route("/accounts") {
        post {
            var account = call.receive<Account>()
            try {
                account = accountService.create(account)
                call.respond(HttpStatusCode.Created, account)
            } catch (e: EntityAlreadyExistsException) {
                call.respond(HttpStatusCode.Conflict, e.message!!)
            }
        }
        authenticate(AuthMethod.JWT.provider) {
            get("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid id")
                    return@get
                }
                val account = accountService.get(id)
                if (account == null) {
                    call.respond(HttpStatusCode.NotFound, "Account with id $id not found")
                } else {
                    call.respond(account)
                }
            }
            get("/?username={username}") {
                val username = call.queryParameters["username"]
                username?.let {
                    val account = accountService.getByUsername(it)
                    account?.let {
                        call.respond(account)
                    } ?: call.respond(HttpStatusCode.NotFound, "Account with username $username not found")
                }
            }
        }
    }
}
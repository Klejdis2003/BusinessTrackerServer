package com.klejdis.services.model

import com.klejdis.services.util.getZonedDateTimeNow
import io.ktor.server.auth.*
import kotlinx.serialization.Serializable

@Serializable
data class LoginSession(
    val id: String,
    val token: String,
    val creationTime: String = getZonedDateTimeNow().toString(),
) : Principal


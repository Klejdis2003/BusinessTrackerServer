package com.klejdis.services.model

import io.ktor.server.auth.*
import kotlinx.serialization.Serializable

@Serializable
data class Session(
    val id: String,
    val token: String,
): Principal


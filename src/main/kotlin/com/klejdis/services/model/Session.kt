package com.klejdis.services.model

import io.ktor.server.auth.*
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class Session(
    val id: String,
    val token: String,
    val creationTime: String = LocalDateTime.now().toString()
) : Principal


package com.klejdis.services.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileInfo(
    val email: String,
    @SerialName("given_name") val name: String? = null,
    @SerialName("family_name") val surname: String? = null,
    val picture: String? = null,
)
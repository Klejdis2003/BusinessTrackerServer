package com.klejdis.services.dto

import com.klejdis.services.model.Business
import kotlinx.serialization.Serializable

@Serializable
data class AccountRequestDto(
    val username: String,
    val password: String,
)

@Serializable
data class BusinessDto(
    val id: Int,
    val ownerEmail: String
)

class BusinessMapper {
    fun toBusinessDto(business: Business): BusinessDto {
        return BusinessDto(
            id = business.id,
            ownerEmail = business.ownerEmail,
        )
    }

}
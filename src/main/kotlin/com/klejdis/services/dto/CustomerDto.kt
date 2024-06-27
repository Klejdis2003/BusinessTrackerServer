package com.klejdis.services.dto

import com.klejdis.services.model.Customer
import kotlinx.serialization.Serializable

@Serializable
data class CustomerDto(
    val id: Int = 0,
    val name: String,
    val phone: String
)

class CustomerMapper {
    fun toCustomerDto(customer: Customer): CustomerDto {
        return CustomerDto(
            id = customer.id,
            name = customer.name,
            phone = customer.phone
        )
    }

    fun toEntity(dto: CustomerDto): Customer {
        return Customer {
            this.id = dto.id
            this.name = dto.name
            this.phone = dto.phone
        }
    }
}

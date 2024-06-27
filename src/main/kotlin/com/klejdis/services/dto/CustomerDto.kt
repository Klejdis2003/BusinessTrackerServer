package com.klejdis.services.dto

import com.klejdis.services.model.Customer
import kotlinx.serialization.Serializable

@Serializable
data class CustomerDto(
    val phone: String,
    val name: String,
)

class CustomerMapper {
    fun toCustomerDto(customer: Customer): CustomerDto {
        return CustomerDto(
            name = customer.name,
            phone = customer.phone
        )
    }

    fun toEntity(dto: CustomerDto): Customer {
        return Customer {
            this.name = dto.name
            this.phone = dto.phone
        }
    }
}

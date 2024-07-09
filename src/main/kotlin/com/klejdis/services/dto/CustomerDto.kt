package com.klejdis.services.dto

import com.klejdis.services.model.Customer
import kotlinx.serialization.Serializable

@Serializable
data class CustomerDto(
    val phone: String,
    val name: String = "",
) {
    companion object {
        fun fromEntity(customer: Customer): CustomerDto {
            return CustomerDto(
                phone = customer.phone,
                name = customer.name
            )
        }

        fun toEntity(customer: CustomerDto): Customer {
            return Customer {
                this.phone = customer.phone
                this.name = customer.name
            }
        }
    }
}

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

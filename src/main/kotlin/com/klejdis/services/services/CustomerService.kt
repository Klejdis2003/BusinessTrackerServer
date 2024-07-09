package com.klejdis.services.services

import com.klejdis.services.model.Customer
import com.klejdis.services.repositories.CustomerRepository

class CustomerService(
    private val customerRepository: CustomerRepository
) : Service<Customer>("Customer") {
    suspend fun create (entity: Customer): Customer {
        return executeCreateBlockWithErrorHandling { customerRepository.create(entity) }
    }
}
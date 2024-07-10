package com.klejdis.services.services

import com.klejdis.services.model.Customer
import com.klejdis.services.repositories.CustomerRepository

class CustomerService(
    private val customerRepository: CustomerRepository,
    loggedInEmail: String
) : Service<Customer>("Customer", loggedInEmail = loggedInEmail) {
    suspend fun create (entity: Customer): Customer {
        return executeCreateBlockWithErrorHandling { customerRepository.create(entity) }
    }
}
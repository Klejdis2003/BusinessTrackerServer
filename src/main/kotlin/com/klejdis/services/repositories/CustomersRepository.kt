package com.klejdis.services.repositories

import com.klejdis.services.model.Customer

interface CustomersRepository: CrudRepository<String, Customer> {
    /**
     * Searches for customers by name.
     * @param name The name to search for
     * @return A list of customers that contain the given name
     */
    fun searchByName(name: String): List<Customer>

    /**
     * Searches for customers by phone number.
     * @param phone The phone number to search for
     * @return A list of customers that contain the given phone number
     */
    fun searchByPhone(phone: String): List<Customer>

    /**
     * Searches for customers by name or phone number.
     * @param any The name or phone number to search for
     * @return A list of customers that contain the given name or phone number
     */
    fun search(any: String): List<Customer> {
        return searchByName(any) + searchByPhone(any)
    }
}
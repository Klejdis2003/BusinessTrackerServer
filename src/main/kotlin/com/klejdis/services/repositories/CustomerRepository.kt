package com.klejdis.services.repositories

import com.klejdis.services.model.Customer

interface CustomerRepository: CrudRepository<String, Customer> {

    /**
     * Retrieves all customers that belong to the given business. A customer belongs to a business if they
     * have at least made one order with that business.
     * @param businessEmail The email of the business to get customers for
     * @return A list of customers that belong to the given business
     */
    suspend fun getByBusiness(businessEmail: String): List<Customer>

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
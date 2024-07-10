package com.klejdis.services.repositories

import com.klejdis.services.config.customers
import com.klejdis.services.filters.CustomerFilterTransformer
import com.klejdis.services.filters.Filter
import com.klejdis.services.model.Businesses
import com.klejdis.services.model.Customer
import com.klejdis.services.model.Customers
import com.klejdis.services.model.Orders
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.add
import org.ktorm.entity.update
import org.ktorm.schema.ColumnDeclaring

class CustomerRepositoryKtorm(private val database: Database): CustomerRepository {
    private fun buildQuery(conditions: List<() -> ColumnDeclaring<Boolean>>): Query {
        return database
            .from(Customers)
            .select()
            .whereWithConditions { conditions.forEach { condition -> it += condition() } }
    }

    private fun fetchQuery(query: Query): List<Customer> {
        return query.map { Customers.createEntity(it) }
    }
    private fun fetchMainQueryWithConditions(filters: Iterable<Filter> = listOf(), additionalConditions : List<() -> ColumnDeclaring<Boolean>> = listOf()): List<Customer> {
        val conditions = CustomerFilterTransformer.generateTransformedFilters(filters)
        return fetchQuery(buildQuery(conditions + additionalConditions))
    }

    private fun fetchMainQueryWithCondition(condition: () -> ColumnDeclaring<Boolean>): List<Customer> {
        return fetchMainQueryWithConditions(additionalConditions = listOf(condition))
    }

    override suspend fun getByBusiness(businessEmail: String): List<Customer> {
        val query = database
            .from(Customers)
            .innerJoin(Orders, on = Orders.customerPhone eq Customers.phone)
            .innerJoin(Businesses , on = Orders.businessId eq Businesses.id)
            .select()
            .where { Businesses.ownerEmail eq businessEmail }

        return fetchQuery(query)
    }


    override fun searchByName(name: String): List<Customer> {
        return fetchMainQueryWithCondition { Customers.name like "%$name%" }
    }

    override fun searchByPhone(phone: String): List<Customer> {
        return fetchMainQueryWithCondition { Customers.phone like "%$phone%" }
    }

    override suspend fun getAll(filters: Iterable<Filter>): List<Customer> {
        return fetchMainQueryWithConditions(filters)
    }

    override suspend fun get(id: String): Customer? {
        return fetchMainQueryWithCondition { Customers.phone eq id }.firstOrNull()
    }

    override suspend fun create(entity: Customer): Customer {
        database.customers.add(entity)
        return entity
    }

    override suspend fun update(entity: Customer): Customer {
        database.customers.update(entity)
        return get(entity.phone)!!
    }

    override suspend fun delete(id: Int): Boolean {
        return database.delete(Customers) { Customers.phone eq id.toString() } > 0
    }

}
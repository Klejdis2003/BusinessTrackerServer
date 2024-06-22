package com.klejdis.services.services

import com.klejdis.services.dto.BusinessMapper
import com.klejdis.services.dto.BusinessDto
import com.klejdis.services.model.Business
import com.klejdis.services.model.ItemDto
import com.klejdis.services.repositories.BusinessRepository
import com.klejdis.services.repositories.ItemRepository
import org.postgresql.util.PSQLException
import org.postgresql.util.PSQLState

class BusinessService(
    private val businessRepository: BusinessRepository,
    private val itemRepository: ItemRepository,
    private val businessMapper: BusinessMapper
)  {

    suspend fun get(id: Int): BusinessDto? {
        val account = businessRepository.get(id)
        account?.let { return businessMapper.toBusinessDto(it) }
        return null
    }

    suspend fun getByEmail(email: String): BusinessDto? {
        val account = businessRepository.getByEmail(email)
        account?.let { return businessMapper.toBusinessDto(it) }
        return null
    }

    suspend fun getBusinessItems(ownerEmail: String): List<ItemDto> {
        val items = itemRepository.getByBusinessOwnerEmail(ownerEmail)
        return items.map { it.toDto() }
    }

    suspend fun create(ownerEmail: String): BusinessDto {
        val business = Business {
            this.ownerEmail = ownerEmail
        }
        try {
            val createdBusiness = businessRepository.create(business)
            return businessMapper.toBusinessDto(createdBusiness)
        }
        catch (e: PSQLException) {
            if (e.sqlState == PSQLState.UNIQUE_VIOLATION.state) {
                throw IllegalArgumentException("Business with email $ownerEmail already exists")
            }
            throw e
        }
    }

    suspend fun createIfNotExists(ownerEmail: String) {
        try {
            create(ownerEmail)
            println("NEW BUSINESS. Email=$ownerEmail")
        }
        catch (_: IllegalArgumentException) { }
    }


    suspend fun update(id: Int): BusinessDto {
        return businessMapper.toBusinessDto(Business())
    }

     suspend fun delete(id: Int): Boolean {
        return false
    }
}
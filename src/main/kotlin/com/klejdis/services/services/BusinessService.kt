package com.klejdis.services.services

import com.klejdis.services.dto.BusinessDto
import com.klejdis.services.dto.BusinessMapper
import com.klejdis.services.dto.ItemDto
import com.klejdis.services.dto.ItemMapper
import com.klejdis.services.model.Business
import com.klejdis.services.repositories.BusinessRepository
import com.klejdis.services.repositories.ItemRepository

class BusinessService(
    private val businessRepository: BusinessRepository,
    private val itemRepository: ItemRepository,
    private val businessMapper: BusinessMapper,
    private val itemMapper: ItemMapper
) : Service<Business>("Business") {

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
        return items.map { itemMapper.toItemDto(it) }
    }

    suspend fun create(ownerEmail: String): BusinessDto {
        val business = Business {
            this.ownerEmail = ownerEmail
        }
        val newBusiness = super.executeCreateBlockWithErrorHandling { businessRepository.create(business) }
        return businessMapper.toBusinessDto(newBusiness)
    }

    suspend fun createIfNotExists(ownerEmail: String) {
        try {
            create(ownerEmail)
            println("NEW BUSINESS. Email=$ownerEmail")
        } catch (_: EntityAlreadyExistsException) {
            println("BUSINESS with email=$ownerEmail already exists. Skipping creation.")
        }
    }


    suspend fun update(id: Int): BusinessDto {
        return businessMapper.toBusinessDto(Business())
    }

    suspend fun delete(id: Int): Boolean {
        return false
    }
}
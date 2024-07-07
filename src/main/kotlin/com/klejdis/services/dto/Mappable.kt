package com.klejdis.services.dto


interface Mappable<T, R> {
    /**
     * Maps an entity to a DTO.
     * @param entity the entity to map
     * @return the DTO
     */
    fun fromEntity(entity: T): R

    /**
     * Maps a DTO to an entity.
     * @param dto the DTO to map
     * @return the entity
     */
    fun toEntity(dto: R): T
}
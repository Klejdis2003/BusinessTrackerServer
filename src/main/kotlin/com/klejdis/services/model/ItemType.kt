package com.klejdis.services.model

import org.ktorm.entity.Entity

interface ItemType: Entity<ItemType> {
    companion object: Entity.Factory<ItemType>()
    val id: Int
    var name: String
    var description: String
}

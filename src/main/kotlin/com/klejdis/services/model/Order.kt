package com.klejdis.services.model

import org.ktorm.entity.Entity
import java.time.LocalDate

interface Order: Entity<Order> {
    companion object: Entity.Factory<Order>()
    var id: Int
    var business: Business
    var date: LocalDate
    var amount: Double
    var items: List<Item>
}
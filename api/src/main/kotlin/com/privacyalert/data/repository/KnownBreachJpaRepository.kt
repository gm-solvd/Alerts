package com.privacyalert.data.repository

import com.privacyalert.data.entity.KnownBreachEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface KnownBreachJpaRepository : JpaRepository<KnownBreachEntity, UUID> {
    fun findByName(name: String): KnownBreachEntity?

    @org.springframework.data.jpa.repository.Query("SELECT k.name FROM KnownBreachEntity k")
    fun findAllNames(): List<String>
}

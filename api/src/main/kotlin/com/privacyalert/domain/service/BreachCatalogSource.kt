package com.privacyalert.domain.service

import com.privacyalert.domain.model.KnownBreach

interface BreachCatalogSource {
    fun fetchCatalog(): List<KnownBreach>

    fun sourceName(): String
}

package com.privacyalert.domain.repository

import com.privacyalert.domain.model.PasteFinding

interface PasteFindingRepository {
    fun existsByPasteUrl(pasteUrl: String): Boolean
    fun save(finding: PasteFinding): PasteFinding
}

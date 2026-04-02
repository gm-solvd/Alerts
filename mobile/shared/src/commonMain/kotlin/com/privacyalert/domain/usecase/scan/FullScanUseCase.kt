package com.privacyalert.domain.usecase.scan

import com.privacyalert.domain.model.Alert
import com.privacyalert.domain.repository.ScanRepository
import com.privacyalert.domain.util.safeApiCall

class FullScanUseCase(private val scanRepository: ScanRepository) {
    suspend operator fun invoke(email: String): Result<List<Alert>> =
        safeApiCall { scanRepository.fullScan(email) }
}

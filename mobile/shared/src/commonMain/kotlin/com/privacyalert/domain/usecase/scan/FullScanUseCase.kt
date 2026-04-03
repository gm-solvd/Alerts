package com.privacyalert.domain.usecase.scan

import com.privacyalert.domain.model.Alert
import com.privacyalert.domain.repository.ScanRepository
import com.privacyalert.domain.util.safeFlow
import kotlinx.coroutines.flow.Flow

class FullScanUseCase(private val scanRepository: ScanRepository) {
    operator fun invoke(email: String): Flow<Result<List<Alert>>> =
        safeFlow { scanRepository.fullScan(email) }
}

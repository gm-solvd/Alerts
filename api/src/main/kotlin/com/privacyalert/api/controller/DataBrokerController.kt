package com.privacyalert.api.controller

import com.privacyalert.api.dto.DataBrokerSiteResponse
import com.privacyalert.api.dto.toResponse
import com.privacyalert.domain.model.AppException
import com.privacyalert.domain.repository.DataBrokerSiteRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/data-brokers")
class DataBrokerController(
    private val dataBrokerSiteRepository: DataBrokerSiteRepository,
) {
    @GetMapping
    fun getAllDataBrokers(): ResponseEntity<List<DataBrokerSiteResponse>> {
        val brokers = dataBrokerSiteRepository.findAllActive()
        return ResponseEntity.ok(brokers.map { it.toResponse() })
    }

    @GetMapping("/{id}")
    fun getDataBrokerById(
        @PathVariable id: UUID,
    ): ResponseEntity<DataBrokerSiteResponse> {
        val broker =
            dataBrokerSiteRepository.findById(id)
                ?: throw AppException.ResourceNotFoundException("DataBrokerSite", id)
        return ResponseEntity.ok(broker.toResponse())
    }
}

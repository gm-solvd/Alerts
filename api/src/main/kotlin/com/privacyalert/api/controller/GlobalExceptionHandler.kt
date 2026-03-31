package com.privacyalert.api.controller

import com.privacyalert.api.dto.ErrorResponse
import com.privacyalert.domain.model.AppException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(AppException.ResourceNotFoundException::class)
    fun handleNotFound(ex: AppException.ResourceNotFoundException): ResponseEntity<ErrorResponse> {
        log.warn("Resource not found: {}", ex.message)
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ErrorResponse(code = "RESOURCE_NOT_FOUND", message = ex.message ?: "Not found"),
        )
    }

    @ExceptionHandler(AppException.ValidationException::class)
    fun handleValidation(ex: AppException.ValidationException): ResponseEntity<ErrorResponse> {
        log.warn("Validation error: {}", ex.message)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorResponse(code = "VALIDATION_ERROR", message = ex.message),
        )
    }

    @ExceptionHandler(AppException.ConflictException::class)
    fun handleConflict(ex: AppException.ConflictException): ResponseEntity<ErrorResponse> {
        log.warn("Conflict: {}", ex.message)
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            ErrorResponse(code = "CONFLICT", message = ex.message),
        )
    }

    @ExceptionHandler(AppException.UnauthorizedException::class)
    fun handleUnauthorized(ex: AppException.UnauthorizedException): ResponseEntity<ErrorResponse> {
        log.warn("Unauthorized: {}", ex.message)
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            ErrorResponse(code = "UNAUTHORIZED", message = ex.message),
        )
    }

    @ExceptionHandler(AppException.ExternalServiceException::class)
    fun handleExternalService(ex: AppException.ExternalServiceException): ResponseEntity<ErrorResponse> {
        log.error("External service failure: {}", ex.message)
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(
            ErrorResponse(code = "EXTERNAL_SERVICE_ERROR", message = ex.message ?: "External service error"),
        )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValid(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val message =
            ex.bindingResult.fieldErrors
                .joinToString("; ") { "${it.field}: ${it.defaultMessage}" }
        log.warn("Validation error: {}", message)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorResponse(code = "VALIDATION_ERROR", message = message),
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception): ResponseEntity<ErrorResponse> {
        log.error("Unexpected error", ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ErrorResponse(code = "INTERNAL_ERROR", message = "An unexpected error occurred"),
        )
    }
}

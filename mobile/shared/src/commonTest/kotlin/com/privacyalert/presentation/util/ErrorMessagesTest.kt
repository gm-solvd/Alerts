package com.privacyalert.presentation.util

import com.privacyalert.domain.model.AppError
import kotlin.test.Test
import kotlin.test.assertEquals

class ErrorMessagesTest {

    @Test
    fun `Unauthorized maps to session expired message`() {
        val result = AppError.Unauthorized().toUserMessage()
        assertEquals("Your session has expired. Please log in again.", result)
    }

    @Test
    fun `NotFound maps to not found message`() {
        val result = AppError.NotFound().toUserMessage()
        assertEquals("The requested data could not be found.", result)
    }

    @Test
    fun `NetworkError maps to no internet message`() {
        val result = AppError.NetworkError().toUserMessage()
        assertEquals(
            "No internet connection. Please check your network and try again.",
            result,
        )
    }

    @Test
    fun `ServerError maps to server error message`() {
        val result = AppError.ServerError().toUserMessage()
        assertEquals("Something went wrong on our end. Please try again later.", result)
    }

    @Test
    fun `Conflict maps to conflict message`() {
        val result = AppError.Conflict().toUserMessage()
        assertEquals("A conflict occurred. Please refresh and try again.", result)
    }

    @Test
    fun `ValidationError maps to invalid request message`() {
        val result = AppError.ValidationError().toUserMessage()
        assertEquals("Invalid request. Please try again.", result)
    }

    @Test
    fun `UnknownError maps to generic message`() {
        val result = AppError.UnknownError().toUserMessage()
        assertEquals("Something went wrong. Please try again.", result)
    }

    @Test
    fun `generic RuntimeException maps to generic message`() {
        val result = RuntimeException("unexpected").toUserMessage()
        assertEquals("Something went wrong. Please try again.", result)
    }

    @Test
    fun `custom message in AppError does not leak to user`() {
        val result = AppError.ServerError("secret internal detail").toUserMessage()
        assertEquals("Something went wrong on our end. Please try again later.", result)
    }
}

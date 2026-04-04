package com.privacyalert.data.remote

import com.privacyalert.domain.model.AppError
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertNull

class StatusToAppErrorTest {

    @Test
    fun `200 OK returns null`() {
        assertNull(statusToAppError(HttpStatusCode.OK))
    }

    @Test
    fun `201 Created returns null`() {
        assertNull(statusToAppError(HttpStatusCode.Created))
    }

    @Test
    fun `204 No Content returns null`() {
        assertNull(statusToAppError(HttpStatusCode.NoContent))
    }

    @Test
    fun `301 Redirect returns null`() {
        assertNull(statusToAppError(HttpStatusCode.MovedPermanently))
    }

    @Test
    fun `401 Unauthorized returns AppError Unauthorized`() {
        assertIs<AppError.Unauthorized>(statusToAppError(HttpStatusCode.Unauthorized))
    }

    @Test
    fun `404 Not Found returns AppError NotFound`() {
        assertIs<AppError.NotFound>(statusToAppError(HttpStatusCode.NotFound))
    }

    @Test
    fun `409 Conflict returns AppError Conflict`() {
        assertIs<AppError.Conflict>(statusToAppError(HttpStatusCode.Conflict))
    }

    @Test
    fun `400 Bad Request returns AppError ValidationError`() {
        assertIs<AppError.ValidationError>(statusToAppError(HttpStatusCode.BadRequest))
    }

    @Test
    fun `403 Forbidden returns AppError ValidationError`() {
        assertIs<AppError.ValidationError>(statusToAppError(HttpStatusCode.Forbidden))
    }

    @Test
    fun `422 Unprocessable Entity returns AppError ValidationError`() {
        assertIs<AppError.ValidationError>(
            statusToAppError(HttpStatusCode.UnprocessableEntity),
        )
    }

    @Test
    fun `429 Too Many Requests returns AppError ValidationError`() {
        assertIs<AppError.ValidationError>(
            statusToAppError(HttpStatusCode.TooManyRequests),
        )
    }

    @Test
    fun `499 boundary returns AppError ValidationError`() {
        assertIs<AppError.ValidationError>(
            statusToAppError(HttpStatusCode(499, "Client Error")),
        )
    }

    @Test
    fun `500 Internal Server Error returns AppError ServerError`() {
        assertIs<AppError.ServerError>(
            statusToAppError(HttpStatusCode.InternalServerError),
        )
    }

    @Test
    fun `502 Bad Gateway returns AppError ServerError`() {
        assertIs<AppError.ServerError>(statusToAppError(HttpStatusCode.BadGateway))
    }

    @Test
    fun `503 Service Unavailable returns AppError ServerError`() {
        assertIs<AppError.ServerError>(
            statusToAppError(HttpStatusCode.ServiceUnavailable),
        )
    }

    @Test
    fun `599 boundary returns AppError ServerError`() {
        assertIs<AppError.ServerError>(
            statusToAppError(HttpStatusCode(599, "Server Error")),
        )
    }
}

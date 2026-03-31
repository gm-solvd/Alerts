package com.privacyalert.config

import com.privacyalert.domain.service.JwtProvider
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import java.util.UUID

class JwtAuthFilterTest {
    private val jwtProvider = mockk<JwtProvider>()
    private val filter = JwtAuthFilter(jwtProvider)

    @AfterEach
    fun cleanup() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `sets authentication when valid Bearer token is present`() {
        val userId = UUID.randomUUID()
        every { jwtProvider.validateAndExtractUserId("valid-token") } returns userId

        val request =
            MockHttpServletRequest().apply {
                addHeader("Authorization", "Bearer valid-token")
            }

        filter.doFilter(request, MockHttpServletResponse(), MockFilterChain())

        val auth = SecurityContextHolder.getContext().authentication
        assertEquals(userId, auth.principal)
    }

    @Test
    fun `does not set authentication when no Authorization header`() {
        val request = MockHttpServletRequest()

        filter.doFilter(request, MockHttpServletResponse(), MockFilterChain())

        assertNull(SecurityContextHolder.getContext().authentication)
    }

    @Test
    fun `does not set authentication when token is invalid`() {
        every { jwtProvider.validateAndExtractUserId("bad-token") } returns null

        val request =
            MockHttpServletRequest().apply {
                addHeader("Authorization", "Bearer bad-token")
            }

        filter.doFilter(request, MockHttpServletResponse(), MockFilterChain())

        assertNull(SecurityContextHolder.getContext().authentication)
    }

    @Test
    fun `does not set authentication when header is not Bearer`() {
        val request =
            MockHttpServletRequest().apply {
                addHeader("Authorization", "Basic dXNlcjpwYXNz")
            }

        filter.doFilter(request, MockHttpServletResponse(), MockFilterChain())

        assertNull(SecurityContextHolder.getContext().authentication)
    }
}

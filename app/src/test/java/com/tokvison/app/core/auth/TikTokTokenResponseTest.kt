package com.tokvison.app.core.auth

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TikTokTokenResponseTest {
    private fun validResponse() = TikTokTokenResponse(
        openId = "user-123",
        accessToken = "access-token",
        expiresInSeconds = 86_400L,
        refreshToken = "refresh-token",
        refreshExpiresInSeconds = 31_536_000L,
        tokenType = "Bearer",
    )

    @Test
    fun acceptsACompleteTokenBundle() {
        assertTrue(validResponse().isSuccess)
    }

    @Test
    fun rejectsMissingOrBlankRequiredIdentifiersAndTokens() {
        assertFalse(validResponse().copy(openId = null).isSuccess)
        assertFalse(validResponse().copy(openId = " ").isSuccess)
        assertFalse(validResponse().copy(accessToken = null).isSuccess)
        assertFalse(validResponse().copy(accessToken = "").isSuccess)
        assertFalse(validResponse().copy(refreshToken = null).isSuccess)
        assertFalse(validResponse().copy(refreshToken = " ").isSuccess)
    }

    @Test
    fun rejectsMissingOrNonPositiveExpirationValues() {
        assertFalse(validResponse().copy(expiresInSeconds = null).isSuccess)
        assertFalse(validResponse().copy(expiresInSeconds = 0L).isSuccess)
        assertFalse(validResponse().copy(refreshExpiresInSeconds = null).isSuccess)
        assertFalse(validResponse().copy(refreshExpiresInSeconds = 0L).isSuccess)
    }

    @Test
    fun rejectsUnsupportedTokenTypeAndErrorResponses() {
        assertFalse(validResponse().copy(tokenType = null).isSuccess)
        assertFalse(validResponse().copy(tokenType = "Basic").isSuccess)
        assertFalse(validResponse().copy(error = "invalid_request").isSuccess)
    }
}

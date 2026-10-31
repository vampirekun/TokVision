package com.tokvison.app.core.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TikTokAuthConfigTest {
    @Test
    fun requestsOnlyTheScopeUsedByTheCurrentAuthFlow() {
        assertEquals("user.info.basic", TikTokAuthConfig.SCOPES)
        assertFalse(TikTokAuthConfig.SCOPES.split(",").contains("video.list"))
    }

    @Test
    fun directTokenExchangeRequiresDebugBuildAndRealSecret() {
        assertNull(TikTokAuthConfig.directTokenExchangeBlockReason(true, "sandbox-client-secret"))
        assertEquals(
            TikTokAuthConfig.RELEASE_TOKEN_BROKER_REQUIRED_MESSAGE,
            TikTokAuthConfig.directTokenExchangeBlockReason(false, "sandbox-client-secret"),
        )
        assertEquals(
            TikTokAuthConfig.DEBUG_CLIENT_SECRET_REQUIRED_MESSAGE,
            TikTokAuthConfig.directTokenExchangeBlockReason(true, "CHANGE_ME"),
        )
        assertEquals(
            TikTokAuthConfig.DEBUG_CLIENT_SECRET_REQUIRED_MESSAGE,
            TikTokAuthConfig.directTokenExchangeBlockReason(true, "  "),
        )
    }

    @Test
    fun releaseBlockReasonDoesNotEchoTheClientSecret() {
        val secret = "do-not-echo-this-value"
        val reason = TikTokAuthConfig.directTokenExchangeBlockReason(false, secret)

        assertTrue(reason != null)
        assertFalse(reason!!.contains(secret))
        assertFalse(reason.contains("refresh_token", ignoreCase = true))
    }
}

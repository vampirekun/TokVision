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
        val clientKey = "sandbox-client-key"
        val redirectUri = "https://example.com/callback/"

        assertNull(
            TikTokAuthConfig.directTokenExchangeBlockReason(
                true, clientKey, "sandbox-client-secret", redirectUri,
            ),
        )
        assertEquals(
            TikTokAuthConfig.RELEASE_TOKEN_BROKER_REQUIRED_MESSAGE,
            TikTokAuthConfig.directTokenExchangeBlockReason(
                false, clientKey, "sandbox-client-secret", redirectUri,
            ),
        )
        assertEquals(
            TikTokAuthConfig.DEBUG_CLIENT_SECRET_REQUIRED_MESSAGE,
            TikTokAuthConfig.directTokenExchangeBlockReason(
                true, clientKey, "CHANGE_ME", redirectUri,
            ),
        )
        assertEquals(
            TikTokAuthConfig.DEBUG_CLIENT_SECRET_REQUIRED_MESSAGE,
            TikTokAuthConfig.directTokenExchangeBlockReason(
                true, clientKey, "  ", redirectUri,
            ),
        )
    }

    @Test
    fun directTokenExchangeRequiresClientKeyAndHttpsRedirectUri() {
        val secret = "sandbox-client-secret"
        val redirectUri = "https://example.com/callback/"

        assertEquals(
            TikTokAuthConfig.DEBUG_CLIENT_CONFIGURATION_REQUIRED_MESSAGE,
            TikTokAuthConfig.directTokenExchangeBlockReason(true, "", secret, redirectUri),
        )
        assertEquals(
            TikTokAuthConfig.DEBUG_CLIENT_CONFIGURATION_REQUIRED_MESSAGE,
            TikTokAuthConfig.directTokenExchangeBlockReason(true, "CHANGE_ME", secret, redirectUri),
        )
        assertEquals(
            TikTokAuthConfig.DEBUG_CLIENT_CONFIGURATION_REQUIRED_MESSAGE,
            TikTokAuthConfig.directTokenExchangeBlockReason(true, "sandbox-client-key", secret, ""),
        )
        assertEquals(
            TikTokAuthConfig.DEBUG_CLIENT_CONFIGURATION_REQUIRED_MESSAGE,
            TikTokAuthConfig.directTokenExchangeBlockReason(
                true, "sandbox-client-key", secret, "http://example.com/callback/",
            ),
        )
        assertEquals(
            TikTokAuthConfig.DEBUG_CLIENT_CONFIGURATION_REQUIRED_MESSAGE,
            TikTokAuthConfig.directTokenExchangeBlockReason(
                true, "sandbox-client-key", secret, "https://CHANGE-ME.github.io/callback/",
            ),
        )
    }

    @Test
    fun releaseBlockReasonDoesNotEchoTheClientSecret() {
        val secret = "do-not-echo-this-value"
        val message = requireNotNull(TikTokAuthConfig.directTokenExchangeBlockReason(
            false, "sandbox-client-key", secret, "https://example.com/callback/",
        ))

        assertTrue(message.isNotBlank())
        assertFalse(message.contains(secret))
        assertFalse(message.contains("refresh_token", ignoreCase = true))
    }
}

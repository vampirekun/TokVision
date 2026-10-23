package com.tokvison.app.core.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class TikTokAuthConfigTest {
    @Test
    fun requestsOnlyTheScopeUsedByTheCurrentAuthFlow() {
        assertEquals("user.info.basic", TikTokAuthConfig.SCOPES)
        assertFalse(TikTokAuthConfig.SCOPES.split(",").contains("video.list"))
    }
}

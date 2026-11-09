package com.tokvison.app.core.auth

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthRepositoryLogoutTest {
    @Test
    fun logoutInvalidatesEncryptedSessionBeforeClearingWebViewData() = runBlocking {
        val events = mutableListOf<String>()
        val tokenStore = FakeTokenStore(
            initialSession = session(),
            onInvalidate = { events += "encrypted-session" },
        )
        val repository = AuthRepository(
            api = NeverCalledAuthClient(),
            tokenStore = tokenStore,
            webViewSessionCleaner = object : WebViewSessionCleaner {
                override suspend fun clear() {
                    events += "webview-session"
                }
            },
        )

        assertEquals(AuthState.LoggedIn("local-user"), repository.state.value)

        repository.logout()

        assertEquals(listOf("encrypted-session", "webview-session"), events)
        assertNull(tokenStore.session)
        assertEquals(AuthState.LoggedOut, repository.state.value)
    }

    @Test
    fun logoutStillClearsWebViewDataWhenEncryptedSessionInvalidationFails() = runBlocking {
        val invalidationFailure = IllegalStateException("encrypted store unavailable")
        val tokenStore = FakeTokenStore(
            initialSession = session(),
            onInvalidate = { throw invalidationFailure },
        )
        var webViewCleared = false
        val repository = AuthRepository(
            api = NeverCalledAuthClient(),
            tokenStore = tokenStore,
            webViewSessionCleaner = object : WebViewSessionCleaner {
                override suspend fun clear() {
                    webViewCleared = true
                }
            },
        )

        val failure = runCatching { repository.logout() }.exceptionOrNull()

        assertSame(invalidationFailure, failure)
        assertTrue(webViewCleared)
        assertEquals(AuthState.LoggedOut, repository.state.value)
    }

    private fun session() = TikTokSession(
        openId = "local-user",
        accessToken = "access-token",
        refreshToken = "refresh-token",
        accessTokenExpiresAtMillis = Long.MAX_VALUE,
    )

    private class FakeTokenStore(
        initialSession: TikTokSession?,
        private val onInvalidate: () -> Unit = {},
    ) : TokenStore {
        var session: TikTokSession? = initialSession
            private set

        override fun save(session: TikTokSession) {
            this.session = session
        }

        override fun load(): TikTokSession? = session

        override fun clear() = invalidate()

        override fun invalidate() {
            onInvalidate()
            session = null
        }
    }

    private class NeverCalledAuthClient : TikTokAuthClient {
        override suspend fun exchangeCodeForToken(
            code: String,
            codeVerifier: String,
        ): TikTokTokenResponse = error("Logout must not call the token endpoint")

        override suspend fun refreshAccessToken(
            refreshToken: String,
        ): TikTokTokenResponse = error("Logout must not refresh tokens")
    }
}

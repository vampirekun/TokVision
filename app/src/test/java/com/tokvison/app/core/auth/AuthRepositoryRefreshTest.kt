package com.tokvison.app.core.auth

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class AuthRepositoryRefreshTest {
    @Test
    fun invalidGrantClearsStoredSessionAndLogsOut() = runBlocking {
        val store = FakeTokenStore(expiredSession())
        val repository = AuthRepository(
            api = FakeTikTokAuthClient(
                refreshResult = {
                    TikTokTokenResponse(
                        error = "invalid_grant",
                        errorDescription = "Refresh token expired",
                    )
                },
            ),
            tokenStore = store,
        )

        assertTrue(repository.state.value is AuthState.LoggedIn)
        assertNull(repository.ensureFreshAccessToken())
        assertNull(store.load())
        assertEquals(AuthState.LoggedOut, repository.state.value)
    }

    @Test
    fun invalidGrantLogsOutInMemoryWhenSecureStoreClearFails() = runBlocking {
        val store = FakeTokenStore(expiredSession(), failClear = true)
        val repository = AuthRepository(
            api = FakeTikTokAuthClient {
                TikTokTokenResponse(
                    error = "invalid_grant",
                    errorDescription = "Refresh token expired",
                )
            },
            tokenStore = store,
        )

        assertNull(repository.ensureFreshAccessToken())
        assertEquals(AuthState.LoggedOut, repository.state.value)
        // The fake store intentionally retains the session because its clear operation failed.
        assertTrue(store.load() != null)
    }

    @Test
    fun transportFailureKeepsStoredSessionForRetry() = runBlocking {
        val session = expiredSession()
        val store = FakeTokenStore(session)
        val repository = AuthRepository(
            api = FakeTikTokAuthClient { throw IOException("network unavailable") },
            tokenStore = store,
        )

        assertNull(repository.ensureFreshAccessToken())
        assertEquals(session, store.load())
        assertTrue(repository.state.value is AuthState.LoggedIn)
    }

    @Test
    fun cancellationPropagatesAndKeepsStoredSession() = runBlocking {
        val session = expiredSession()
        val store = FakeTokenStore(session)
        val repository = AuthRepository(
            api = FakeTikTokAuthClient { throw CancellationException("refresh cancelled") },
            tokenStore = store,
        )

        try {
            repository.ensureFreshAccessToken()
            throw AssertionError("Expected refresh cancellation to propagate")
        } catch (expected: CancellationException) {
            assertEquals("refresh cancelled", expected.message)
        }

        assertEquals(session, store.load())
        assertTrue(repository.state.value is AuthState.LoggedIn)
    }

    @Test
    fun successfulRefreshReplacesTokensAndReturnsNewAccessToken() = runBlocking {
        val store = FakeTokenStore(expiredSession())
        val repository = AuthRepository(
            api = FakeTikTokAuthClient {
                TikTokTokenResponse(
                    openId = "user-123",
                    accessToken = "new-access",
                    expiresInSeconds = 3600,
                    refreshToken = "new-refresh",
                    refreshExpiresInSeconds = 7200,
                    tokenType = "Bearer",
                )
            },
            tokenStore = store,
        )

        assertEquals("new-access", repository.ensureFreshAccessToken())
        assertEquals("new-refresh", store.load()?.refreshToken)
        assertTrue(repository.state.value is AuthState.LoggedIn)
    }

    private fun expiredSession() = TikTokSession(
        openId = "user-123",
        accessToken = "expired-access",
        refreshToken = "refresh-token",
        accessTokenExpiresAtMillis = 0L,
    )

    private class FakeTokenStore(
        initialSession: TikTokSession?,
        private val failClear: Boolean = false,
    ) : TokenStore {
        private var session = initialSession

        override fun save(session: TikTokSession) {
            this.session = session
        }

        override fun load(): TikTokSession? = session

        override fun clear() {
            if (failClear) throw IllegalStateException("secure storage unavailable")
            session = null
        }
    }

    private class FakeTikTokAuthClient(
        private val refreshResult: suspend () -> TikTokTokenResponse,
    ) : TikTokAuthClient {
        override suspend fun exchangeCodeForToken(
            code: String,
            codeVerifier: String,
        ): TikTokTokenResponse = error("Not used by refresh tests")

        override suspend fun refreshAccessToken(refreshToken: String): TikTokTokenResponse =
            refreshResult()
    }
}

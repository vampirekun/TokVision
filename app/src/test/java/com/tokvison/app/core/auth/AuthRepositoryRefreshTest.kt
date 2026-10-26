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
    fun invalidGrantDoesNotRestoreRejectedSessionAfterSecureCleanupFails() = runBlocking {
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

        var invalidationFailed = false
        try {
            repository.ensureFreshAccessToken()
        } catch (expected: SessionInvalidationException) {
            invalidationFailed = true
        }
        assertTrue(invalidationFailed)
        assertEquals(AuthState.LoggedOut, repository.state.value)

        // The encrypted record remains in this fake, but its persisted invalidation marker
        // makes it unavailable to a newly constructed repository.
        assertTrue(store.rawSessionForTest() != null)
        assertTrue(store.isInvalidatedForTest())
        assertNull(store.load())

        val restoredRepository = AuthRepository(
            api = FakeTikTokAuthClient { throw IOException("Must not refresh an invalidated session") },
            tokenStore = store,
        )
        assertEquals(AuthState.LoggedOut, restoredRepository.state.value)
    }

    @Test
    fun invalidationClearsEncryptedSessionWhenFirstMarkerWriteFails() {
        var markerAttempts = 0
        var clearAttempts = 0
        var sessionPresent = true

        invalidateStoredSession(
            writeInvalidationMarker = {
                markerAttempts++
                false
            },
            clearEncryptedSession = {
                clearAttempts++
                sessionPresent = false
                true
            },
        )

        assertTrue(!sessionPresent)
        assertEquals(1, markerAttempts)
        assertEquals(1, clearAttempts)
    }

    @Test
    fun invalidationRetriesMarkerAndCleanupWhenBothFirstAttemptsFail() {
        var markerAttempts = 0
        var clearAttempts = 0
        var sessionPresent = true

        invalidateStoredSession(
            writeInvalidationMarker = {
                markerAttempts++
                markerAttempts >= 2
            },
            clearEncryptedSession = {
                clearAttempts++
                if (clearAttempts >= 2) {
                    sessionPresent = false
                    true
                } else {
                    false
                }
            },
        )

        assertTrue(!sessionPresent)
        assertEquals(2, markerAttempts)
        assertEquals(2, clearAttempts)
    }

    @Test
    fun invalidationReportsFailureWhenMarkerAndCleanupBothFailTwice() {
        var markerAttempts = 0
        var clearAttempts = 0
        var failed = false

        try {
            invalidateStoredSession(
                writeInvalidationMarker = {
                    markerAttempts++
                    false
                },
                clearEncryptedSession = {
                    clearAttempts++
                    false
                },
            )
        } catch (expected: SessionInvalidationException) {
            failed = true
        }

        assertTrue(failed)
        assertEquals(2, markerAttempts)
        assertEquals(2, clearAttempts)
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
        private var invalidated = false

        override fun save(session: TikTokSession) {
            this.session = session
            invalidated = false
        }

        override fun load(): TikTokSession? = if (invalidated) null else session

        override fun clear() = invalidate()

        override fun invalidate() {
            // Model the durable tombstone being written before encrypted cleanup fails.
            invalidated = true
            if (failClear) throw SessionInvalidationException("secure storage unavailable")
            session = null
        }

        fun rawSessionForTest(): TikTokSession? = session

        fun isInvalidatedForTest(): Boolean = invalidated
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

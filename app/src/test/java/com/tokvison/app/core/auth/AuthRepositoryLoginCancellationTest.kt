package com.tokvison.app.core.auth

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Test

class AuthRepositoryLoginCancellationTest {
    @Test
    fun cancellationDuringCodeExchangePropagatesWithoutPublishingLoginError() = runBlocking {
        val store = FakeTokenStore()
        val repository = AuthRepository(
            api = FakeTikTokAuthClient { _, _ -> throw CancellationException("login cancelled") },
            tokenStore = store,
        )
        val pending = repository.beginLogin()

        try {
            repository.completeLogin("authorization-code", pending.state)
            fail("Expected login cancellation to propagate")
        } catch (expected: CancellationException) {
            assertEquals("login cancelled", expected.message)
        }

        assertEquals(AuthState.LoggedOut, repository.state.value)
        assertNull(store.load())
    }

    private class FakeTokenStore : TokenStore {
        private var session: TikTokSession? = null

        override fun save(session: TikTokSession) {
            this.session = session
        }

        override fun load(): TikTokSession? = session

        override fun clear() {
            session = null
        }

        override fun invalidate() {
            session = null
        }
    }

    private class FakeTikTokAuthClient(
        private val exchangeResult: suspend (String, String) -> TikTokTokenResponse,
    ) : TikTokAuthClient {
        override suspend fun exchangeCodeForToken(
            code: String,
            codeVerifier: String,
        ): TikTokTokenResponse = exchangeResult(code, codeVerifier)

        override suspend fun refreshAccessToken(refreshToken: String): TikTokTokenResponse =
            error("Refresh is not used by this test")
    }
}

package com.tokvison.app.ui.login

import com.tokvison.app.core.auth.AuthRepository
import com.tokvison.app.core.auth.AuthState
import com.tokvison.app.core.auth.PendingLogin
import com.tokvison.app.core.auth.TikTokAuthClient
import com.tokvison.app.core.auth.TikTokSession
import com.tokvison.app.core.auth.TikTokTokenResponse
import com.tokvison.app.core.auth.TokenStore
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginViewModelLifecycleTest {
    @Test
    fun reopeningAfterCancellationCreatesANewPendingAttempt() {
        val first = pendingLogin("first-state")
        val second = pendingLogin("second-state")
        val attempts = mutableListOf(first, second)
        val repository = AuthRepository(
            api = NeverCalledAuthClient(),
            tokenStore = FakeTokenStore(),
            pendingLoginFactory = { attempts.removeAt(0) },
        )
        val viewModel = LoginViewModel(repository, configurationBlockReason = { null })

        viewModel.startLogin()
        assertEquals(first, viewModel.pendingLogin)

        viewModel.cancel()
        assertNull(viewModel.pendingLogin)

        viewModel.startLogin()
        assertEquals(second, viewModel.pendingLogin)
    }

    @Test
    fun beginningANewAttemptClearsThePreviousAuthorizationError() {
        val attempt = pendingLogin("fresh-state")
        val repository = AuthRepository(
            api = NeverCalledAuthClient(),
            tokenStore = FakeTokenStore(),
            pendingLoginFactory = { attempt },
        )
        val viewModel = LoginViewModel(repository, configurationBlockReason = { null })
        repository.reportAuthorizationError("previous attempt failed")

        viewModel.startLogin()

        assertEquals(AuthState.LoggedOut, repository.state.value)
        assertEquals(attempt, viewModel.pendingLogin)
    }

    @Test
    fun blockedConfigurationDoesNotCreateOrExposeAPendingAttempt() {
        var factoryCalls = 0
        val repository = AuthRepository(
            api = NeverCalledAuthClient(),
            tokenStore = FakeTokenStore(),
            pendingLoginFactory = {
                factoryCalls += 1
                pendingLogin("must-not-be-created")
            },
        )
        val viewModel = LoginViewModel(repository, configurationBlockReason = { "token broker required" })

        viewModel.startLogin()

        assertNull(viewModel.pendingLogin)
        assertEquals(AuthState.Error("token broker required"), repository.state.value)
        assertEquals(0, factoryCalls)
        assertFalse(viewModel.canRetry())
    }

    @Test
    fun staleStateFromPreviousAttemptCannotReachTheTokenEndpoint() = runBlocking {
        val first = pendingLogin("old-state")
        val second = pendingLogin("current-state")
        val api = NeverCalledAuthClient()
        val attempts = mutableListOf(first, second)
        val repository = AuthRepository(
            api = api,
            tokenStore = FakeTokenStore(),
            pendingLoginFactory = { attempts.removeAt(0) },
        )

        repository.beginLogin()
        repository.beginLogin()
        val result = repository.completeLogin("stale-code", first.state)

        assertTrue(result.isFailure)
        assertEquals(0, api.exchangeCalls)
        assertEquals(
            "State mismatch or missing pending login",
            result.exceptionOrNull()?.message,
        )
    }

    private fun pendingLogin(state: String) = PendingLogin(
        authorizationUrl = "https://auth.example.com/callback?state=$state",
        codeVerifier = "synthetic-verifier-$state",
        state = state,
    )

    private class FakeTokenStore : TokenStore {
        override fun save(session: TikTokSession) = Unit
        override fun load(): TikTokSession? = null
        override fun clear() = Unit
        override fun invalidate() = Unit
    }

    private class NeverCalledAuthClient : TikTokAuthClient {
        var exchangeCalls = 0

        override suspend fun exchangeCodeForToken(
            code: String,
            codeVerifier: String,
        ): TikTokTokenResponse {
            exchangeCalls += 1
            error("Token endpoint must not be called by this regression test")
        }

        override suspend fun refreshAccessToken(refreshToken: String): TikTokTokenResponse =
            error("Refresh endpoint must not be called by this regression test")
    }
}

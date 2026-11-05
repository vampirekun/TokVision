package com.tokvison.app.core.auth

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TikTokAuthApiTest {
    @Test
    fun releaseAuthorizationCodeExchangeAndRefreshFailBeforeHttpRequest() = runBlocking {
        var requestCount = 0
        val client = HttpClient(
            MockEngine {
                requestCount += 1
                error("A blocked release token operation must not reach the HTTP engine.")
            },
        )

        try {
            val api = TikTokAuthApi(client) {
                TikTokAuthConfig.RELEASE_TOKEN_BROKER_REQUIRED_MESSAGE
            }

            val exchangeFailure = try {
                api.exchangeCodeForToken("synthetic-authorization-code", "synthetic-pkce-verifier")
                null
            } catch (failure: IllegalStateException) {
                failure
            }

            assertNotNull(exchangeFailure)
            assertEquals(
                TikTokAuthConfig.RELEASE_TOKEN_BROKER_REQUIRED_MESSAGE,
                exchangeFailure?.message,
            )
            assertEquals(0, requestCount)

            val refreshFailure = try {
                api.refreshAccessToken("synthetic-refresh-token")
                null
            } catch (failure: IllegalStateException) {
                failure
            }

            assertNotNull(refreshFailure)
            assertEquals(
                TikTokAuthConfig.RELEASE_TOKEN_BROKER_REQUIRED_MESSAGE,
                refreshFailure?.message,
            )
            assertEquals(0, requestCount)
        } finally {
            client.close()
        }
    }

    @Test
    fun authorizationCodeExchangeAndRefreshHaveBoundedRequestTimeouts() = runBlocking {
        val client = HttpClient(
            MockEngine {
                delay(200)
                error("A timed-out token request must not wait for this response.")
            },
        ) {
            configureTikTokAuthClient(
                requestTimeoutMillis = 25,
                connectTimeoutMillis = 1_000,
                socketTimeoutMillis = 1_000,
            )
        }

        try {
            val api = TikTokAuthApi(client) { null }

            val exchangeFailure = try {
                api.exchangeCodeForToken("synthetic-authorization-code", "synthetic-pkce-verifier")
                null
            } catch (failure: Throwable) {
                failure
            }
            assertTrue(
                "Expected authorization-code exchange to time out, got $exchangeFailure",
                exchangeFailure is HttpRequestTimeoutException,
            )

            val refreshFailure = try {
                api.refreshAccessToken("synthetic-refresh-token")
                null
            } catch (failure: Throwable) {
                failure
            }
            assertTrue(
                "Expected refresh request to time out, got $refreshFailure",
                refreshFailure is HttpRequestTimeoutException,
            )
        } finally {
            client.close()
        }
    }
}

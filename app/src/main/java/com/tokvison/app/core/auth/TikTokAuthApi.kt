package com.tokvison.app.core.auth

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.submitForm
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Parameters
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

internal const val AUTH_REQUEST_TIMEOUT_MILLIS = 20_000L
internal const val AUTH_CONNECT_TIMEOUT_MILLIS = 10_000L
internal const val AUTH_SOCKET_TIMEOUT_MILLIS = 20_000L

/**
 * Keep the authentication transport bounded. The same configuration function is used by the
 * Android client and the MockEngine regression so the timeout policy cannot silently diverge.
 */
internal fun <T : HttpClientEngineConfig> HttpClientConfig<T>.configureTikTokAuthClient(
    requestTimeoutMillis: Long = AUTH_REQUEST_TIMEOUT_MILLIS,
    connectTimeoutMillis: Long = AUTH_CONNECT_TIMEOUT_MILLIS,
    socketTimeoutMillis: Long = AUTH_SOCKET_TIMEOUT_MILLIS,
) {
    install(HttpTimeout) {
        this.requestTimeoutMillis = requestTimeoutMillis
        this.connectTimeoutMillis = connectTimeoutMillis
        this.socketTimeoutMillis = socketTimeoutMillis
    }
    install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
}

/** Thin wrapper around the two Login Kit token endpoints. Direct exchange is debug-only:
 * release builds must use a backend/token broker so the app secret and refresh tokens remain
 * server-side. No feed/search calls live here (see KNOWN_LIMITATIONS.md). */
interface TikTokAuthClient {
    suspend fun exchangeCodeForToken(code: String, codeVerifier: String): TikTokTokenResponse
    suspend fun refreshAccessToken(refreshToken: String): TikTokTokenResponse
}

class TikTokAuthApi(
    private val httpClient: HttpClient = defaultClient(),
    private val blockReasonProvider: () -> String? = TikTokAuthConfig::directTokenExchangeBlockReason,
) : TikTokAuthClient {
    override suspend fun exchangeCodeForToken(code: String, codeVerifier: String): TikTokTokenResponse {
        requireDirectTokenExchangeAllowed()
        val response = httpClient.submitForm(
            url = TikTokAuthConfig.TOKEN_URL,
            formParameters = Parameters.build {
                append("client_key", TikTokAuthConfig.clientKey)
                append("client_secret", TikTokAuthConfig.clientSecret)
                append("code", code)
                append("grant_type", "authorization_code")
                append("redirect_uri", TikTokAuthConfig.redirectUri)
                append("code_verifier", codeVerifier)
            },
        )
        return json.decodeFromString(TikTokTokenResponse.serializer(), response.bodyAsText())
    }

    override suspend fun refreshAccessToken(refreshToken: String): TikTokTokenResponse {
        requireDirectTokenExchangeAllowed()
        val response = httpClient.submitForm(
            url = TikTokAuthConfig.TOKEN_URL,
            formParameters = Parameters.build {
                append("client_key", TikTokAuthConfig.clientKey)
                append("client_secret", TikTokAuthConfig.clientSecret)
                append("grant_type", "refresh_token")
                append("refresh_token", refreshToken)
            },
        )
        return json.decodeFromString(TikTokTokenResponse.serializer(), response.bodyAsText())
    }

    private fun requireDirectTokenExchangeAllowed() {
        val blockReason = blockReasonProvider()
        if (blockReason != null) {
            throw IllegalStateException(blockReason)
        }
    }

    private companion object {
        val json = Json { ignoreUnknownKeys = true }

        fun defaultClient() = HttpClient(Android) {
            configureTikTokAuthClient()
        }
    }
}

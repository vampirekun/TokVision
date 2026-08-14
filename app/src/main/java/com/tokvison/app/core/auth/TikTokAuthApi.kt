package com.tokvison.app.core.auth

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.submitForm
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Parameters
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/** Thin wrapper around the two Login Kit token endpoints. No feed/search calls live here —
 * those don't exist officially (see KNOWN_LIMITATIONS.md). This only ever talks to
 * open.tiktokapis.com's OAuth token endpoint. */
class TikTokAuthApi(
    private val httpClient: HttpClient = defaultClient(),
) {
    suspend fun exchangeCodeForToken(code: String, codeVerifier: String): TikTokTokenResponse {
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

    suspend fun refreshAccessToken(refreshToken: String): TikTokTokenResponse {
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

    private companion object {
        val json = Json { ignoreUnknownKeys = true }

        fun defaultClient() = HttpClient(Android) {
            install(ContentNegotiation) { json(json) }
        }
    }
}

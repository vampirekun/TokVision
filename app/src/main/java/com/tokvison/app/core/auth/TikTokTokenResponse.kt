package com.tokvison.app.core.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Mirrors the response body documented at /doc/oauth-user-access-token-management. */
@Serializable
data class TikTokTokenResponse(
    @SerialName("open_id") val openId: String? = null,
    @SerialName("scope") val scope: String? = null,
    @SerialName("access_token") val accessToken: String? = null,
    @SerialName("expires_in") val expiresInSeconds: Long? = null,
    @SerialName("refresh_token") val refreshToken: String? = null,
    @SerialName("refresh_expires_in") val refreshExpiresInSeconds: Long? = null,
    @SerialName("token_type") val tokenType: String? = null,
    // Present instead of the fields above when the request failed.
    @SerialName("error") val error: String? = null,
    @SerialName("error_description") val errorDescription: String? = null,
    @SerialName("log_id") val logId: String? = null,
) {
    val isSuccess: Boolean get() = error == null && accessToken != null
}

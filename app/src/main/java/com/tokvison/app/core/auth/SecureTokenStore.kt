package com.tokvison.app.core.auth

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/** A session as far as the rest of the app is concerned — no tokens leak past [AuthRepository]. */
data class TikTokSession(
    val openId: String,
    val accessToken: String,
    val refreshToken: String,
    val accessTokenExpiresAtMillis: Long,
)

/**
 * Tokens are the most sensitive thing this app stores — they live in
 * [EncryptedSharedPreferences] (AES-256, Keystore-backed master key), never in plain
 * `SharedPreferences`, never logged, never included in crash reports.
 */
class SecureTokenStore(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "tokvison_secure_session",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    fun save(session: TikTokSession) {
        prefs.edit()
            .putString(KEY_OPEN_ID, session.openId)
            .putString(KEY_ACCESS_TOKEN, session.accessToken)
            .putString(KEY_REFRESH_TOKEN, session.refreshToken)
            .putLong(KEY_EXPIRES_AT, session.accessTokenExpiresAtMillis)
            .apply()
    }

    fun load(): TikTokSession? {
        val openId = prefs.getString(KEY_OPEN_ID, null) ?: return null
        val accessToken = prefs.getString(KEY_ACCESS_TOKEN, null) ?: return null
        val refreshToken = prefs.getString(KEY_REFRESH_TOKEN, null) ?: return null
        val expiresAt = prefs.getLong(KEY_EXPIRES_AT, 0L)
        return TikTokSession(openId, accessToken, refreshToken, expiresAt)
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val KEY_OPEN_ID = "open_id"
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val KEY_EXPIRES_AT = "access_token_expires_at"
    }
}

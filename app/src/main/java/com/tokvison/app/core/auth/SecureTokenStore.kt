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
 * EncryptedSharedPreferences (AES-256, Keystore-backed master key), never in plain
 * SharedPreferences, never logged, never included in crash reports.
 *
 * The separate preferences file stores only a non-secret invalidation marker. It is written
 * before encrypted-session cleanup so a failed cleanup cannot resurrect a rejected refresh
 * token on the next process start.
 */
interface TokenStore {
    fun save(session: TikTokSession)
    fun load(): TikTokSession?
    fun clear()
    fun invalidate()
}

/**
 * Fail closed even if one persistence operation fails: always attempt encrypted cleanup after
 * the marker write, then retry the failed route once before reporting an unrecoverable failure.
 * If cleanup succeeds, a marker is not required because no rejected session remains to restore.
 */
internal fun invalidateStoredSession(
    writeInvalidationMarker: () -> Boolean,
    clearEncryptedSession: () -> Boolean,
) {
    val markerWritten = runCatching { writeInvalidationMarker() }.getOrDefault(false)
    val sessionCleared = runCatching { clearEncryptedSession() }.getOrDefault(false)
    if (sessionCleared) return

    if (markerWritten) {
        val clearedOnRetry = runCatching { clearEncryptedSession() }.getOrDefault(false)
        if (clearedOnRetry) return
        throw IllegalStateException("Unable to clear encrypted session; invalidation marker remains set.")
    }

    val markerWrittenOnRetry = runCatching { writeInvalidationMarker() }.getOrDefault(false)
    val clearedOnRetry = runCatching { clearEncryptedSession() }.getOrDefault(false)
    if (clearedOnRetry) return
    if (markerWrittenOnRetry) {
        throw IllegalStateException("Unable to clear encrypted session; invalidation marker remains set.")
    }

    throw IllegalStateException("Unable to invalidate session: marker persistence and encrypted cleanup failed twice.")
}

class SecureTokenStore(context: Context) : TokenStore {
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

    // This file contains only a boolean tombstone, never tokens or user identifiers.
    private val sessionFlags = context.applicationContext.getSharedPreferences(
        "tokvison_session_state",
        Context.MODE_PRIVATE,
    )

    override fun save(session: TikTokSession) {
        val saved = prefs.edit()
            .putString(KEY_OPEN_ID, session.openId)
            .putString(KEY_ACCESS_TOKEN, session.accessToken)
            .putString(KEY_REFRESH_TOKEN, session.refreshToken)
            .putLong(KEY_EXPIRES_AT, session.accessTokenExpiresAtMillis)
            .commit()
        check(saved) { "Unable to persist encrypted session" }

        // Clear the tombstone only after the replacement session is durably stored.
        val markerCleared = sessionFlags.edit().remove(KEY_SESSION_INVALIDATED).commit()
        check(markerCleared) { "Unable to clear session invalidation marker" }
    }

    override fun load(): TikTokSession? {
        if (sessionFlags.getBoolean(KEY_SESSION_INVALIDATED, false)) return null

        val openId = prefs.getString(KEY_OPEN_ID, null) ?: return null
        val accessToken = prefs.getString(KEY_ACCESS_TOKEN, null) ?: return null
        val refreshToken = prefs.getString(KEY_REFRESH_TOKEN, null) ?: return null
        val expiresAt = prefs.getLong(KEY_EXPIRES_AT, 0L)
        return TikTokSession(openId, accessToken, refreshToken, expiresAt)
    }

    override fun clear() = invalidate()

    override fun invalidate() {
        invalidateStoredSession(
            writeInvalidationMarker = {
                sessionFlags.edit().putBoolean(KEY_SESSION_INVALIDATED, true).commit()
            },
            clearEncryptedSession = { prefs.edit().clear().commit() },
        )
    }

    private companion object {
        const val KEY_OPEN_ID = "open_id"
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val KEY_EXPIRES_AT = "access_token_expires_at"
        const val KEY_SESSION_INVALIDATED = "session_invalidated"
    }
}

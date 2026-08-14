package com.tokvison.app.core.auth

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * RFC 7636 (PKCE) + a CSRF `state` token. Required for mobile/TV OAuth clients because they
 * can't keep a `client_secret` truly confidential the way a server-side web app can — PKCE
 * proves the app that started the auth request is the same one redeeming the code.
 */
object PkceUtil {
    private val secureRandom = SecureRandom()

    /** 43–128 char unreserved-charset string, per RFC 7636 §4.1. */
    fun generateCodeVerifier(): String {
        val bytes = ByteArray(64)
        secureRandom.nextBytes(bytes)
        return base64UrlEncode(bytes)
    }

    /** S256 challenge derived from [codeVerifier], per RFC 7636 §4.2. */
    fun generateCodeChallenge(codeVerifier: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(codeVerifier.toByteArray(Charsets.US_ASCII))
        return base64UrlEncode(digest)
    }

    /** Anti-forgery token echoed back by TikTok; must be verified on callback. */
    fun generateState(): String {
        val bytes = ByteArray(24)
        secureRandom.nextBytes(bytes)
        return base64UrlEncode(bytes)
    }

    private fun base64UrlEncode(bytes: ByteArray): String =
        Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
}
